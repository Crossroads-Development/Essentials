package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.tileentity.BlockEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

@ObjectHolder(Essentials.MODID)
public class RedstoneTransmitterBlockEntity extends BlockEntity implements ILinkTE{

	@ObjectHolder("redstone_transmitter")
	public static BlockEntityType<RedstoneTransmitterBlockEntity> TYPE = null;

	public final LinkHelper linkHelper = new LinkHelper(this);

	private boolean builtConnections = false;
	//The current output, regardless of a pending update
	private float output;

	public RedstoneTransmitterBlockEntity(){
		super(TYPE);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return linkHelper.frustrum();
	}

	@Override
	public boolean canBeginLinking(){
		return true;
	}

	public void dye(DyeColor color){
		if(getBlockState().getValue(ESProperties.COLOR) != color){
			level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(ESProperties.COLOR, color));

			for(BlockPos link : linkHelper.getLinksAbsolute()){
				BlockState linkState = level.getBlockState(link);
				if(linkState.getBlock() == ESBlocks.redstoneReceiver){
					level.setBlockAndUpdate(link, linkState.setValue(ESProperties.COLOR, color));
				}
			}
		}
	}

	public float getOutput(){
		if(!builtConnections){
			buildConnections();
		}
		return output;
	}

	@Override
	public void removeLinkSource(BlockPos end){
		linkHelper.removeLink(end);
	}

	public void buildConnections(){
		//Rebuild the sources list

		if(!level.isClientSide){
			builtConnections = true;
			ArrayList<Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Direction>> preSrc = new ArrayList<>(sources.size());
			preSrc.addAll(sources);
			//Wipe old sources
			sources.clear();

			for(Direction checkDir : Direction.values()){
				BlockEntity te = level.getBlockEntity(worldPosition.relative(checkDir));
				IRedstoneHandler otherHandler;
				if(te != null && (otherHandler = BlockUtil.get(te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, checkDir.getOpposite()))) != null){
					otherHandler.requestSrc(circRef, 0, checkDir.getOpposite(), checkDir);
				}
			}

			//if sources changed, schedule an update
			if(sources.size() != preSrc.size() || !sources.containsAll(preSrc)){
				level.getBlockTicks().scheduleTick(worldPosition, ESBlocks.redstoneTransmitter, RedstoneUtil.DELAY, TickPriority.NORMAL);
			}
		}
	}

	public void refreshOutput(){
		//Immediately recalculates the output, without a 2-tick delay
		if(!builtConnections){
			buildConnections();//Can be needed when reloading
		}

		float input = 0;
		Direction[] sidesToCheck = Direction.values();//Don't check sides for vanilla redstone w/ a circuit

		for(int i = 0; i < sources.size(); i++){
			Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Direction> ref = sources.get(i);
			IRedstoneHandler handl;
			//Remove invalid entries to speed up future checks
			if(ref == null || (handl = BlockUtil.get(ref.getLeft().get())) == null){
				sources.remove(i);
				i--;
				continue;
			}

			sidesToCheck[ref.getRight().get3DDataValue()] = null;
			input = RedstoneUtil.chooseInput(input, RedstoneUtil.sanitize(handl.getOutput()));
		}

		//Any input without a circuit input uses vanilla redstone instead
		//Don't check any side with a circuit
		for(Direction dir : sidesToCheck){
			if(dir != null){
				input = RedstoneUtil.chooseInput(input, RedstoneUtil.getRedstoneOnSide(level, worldPosition, dir));
			}
		}

		input = RedstoneUtil.sanitize(input);

		if(RedstoneUtil.didChange(output, input)){
			output = input;
			for(BlockPos link : linkHelper.getLinksAbsolute()){
				BlockEntity te = level.getBlockEntity(link);
				if(te instanceof RedstoneReceiverBlockEntity){
					((RedstoneReceiverBlockEntity) te).notifyOutputChange();
				}
			}
			setChanged();
		}
	}

	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt = super.getUpdateTag();
		linkHelper.writeNBT(nbt);
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);
		output = nbt.getFloat("out");
		linkHelper.readNBT(nbt);
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.putFloat("out", output);
		linkHelper.writeNBT(nbt);
		return nbt;
	}

	@Override
	public BlockEntity getTE(){
		return this;
	}

	@Override
	public Color getColor(){
		return new Color(getBlockState().getValue(ESProperties.COLOR).getColorValue());
	}

	@Override
	public boolean canLink(ILinkTE otherTE){
		return otherTE instanceof RedstoneReceiverBlockEntity;
	}

	@Override
	public Set<BlockPos> getLinks(){
		return linkHelper.getLinksRelative();
	}

	@Override
	public int getRange(){
		return ESConfig.wirelessRange.get();
	}

	@Override
	public int getMaxLinks(){
		return 64;
	}

	@Override
	public boolean createLinkSource(ILinkTE endpoint, @Nullable Player player){
		return linkHelper.addLink(endpoint, player);
	}

	@Override
	public void receiveLong(byte identifier, long message, @Nullable ServerPlayer sendingPlayer){
		linkHelper.handleIncomingPacket(identifier, message);
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		circOpt.invalidate();
	}

	private final LazyOptional<IRedstoneHandler> circOpt = LazyOptional.of(CircuitHandler::new);
	private WeakReference<LazyOptional<IRedstoneHandler>> circRef = new WeakReference<>(circOpt);

	private final ArrayList<Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Direction>> sources = new ArrayList<>(1);

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
		if(cap == RedstoneUtil.REDSTONE_CAPABILITY){
			return (LazyOptional<T>) circOpt;
		}
		return super.getCapability(cap, side);
	}

	private class CircuitHandler implements IRedstoneHandler{

		@Override
		public float getOutput(){
			return 0;
		}

		@Override
		public void findDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide){
			LazyOptional<IRedstoneHandler> srcOption = src.get();
			if(srcOption != null && srcOption.isPresent()){
				IRedstoneHandler srcHandler = BlockUtil.get(srcOption);
				srcHandler.addDependent(circRef, nominalSide);
				Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Direction> srcPair = Pair.of(src, fromSide);
				if(!sources.contains(srcPair)){
					sources.add(srcPair);
				}
			}
		}

		@Override
		public void requestSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide){
			//No-Op
		}

		@Override
		public void addSrc(WeakReference<LazyOptional<IRedstoneHandler>> src, Direction fromSide){
			Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Direction> srcPair = Pair.of(src, fromSide);
			if(!sources.contains(srcPair)){
				sources.add(srcPair);
				notifyInputChange(src);
			}
		}

		@Override
		public void addDependent(WeakReference<LazyOptional<IRedstoneHandler>> dependent, Direction toSide){
			//No-Op
		}

		@Override
		public void notifyInputChange(WeakReference<LazyOptional<IRedstoneHandler>> src){
			level.getBlockTicks().scheduleTick(worldPosition, ESBlocks.redstoneTransmitter, RedstoneUtil.DELAY, TickPriority.HIGH);
		}
	}
}
