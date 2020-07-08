package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.packets.SendLongToClient;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@ObjectHolder(Essentials.MODID)
public class RedstoneTransmitterTileEntity extends TileEntity implements ILinkTE{

	@ObjectHolder("redstone_transmitter")
	public static TileEntityType<RedstoneTransmitterTileEntity> TYPE = null;

	private Set<BlockPos> linked = new HashSet<>();


	private boolean builtConnections = false;
	//The current output, regardless of a pending update
	private float output;

	public RedstoneTransmitterTileEntity(){
		super(TYPE);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		return ILinkTE.frustrum(this);
	}

	@Override
	public boolean canBeginLinking(){
		return true;
	}

	public void dye(DyeColor color){
		if(world.getBlockState(pos).get(ESProperties.COLOR) != color){
			world.setBlockState(pos, world.getBlockState(pos).with(ESProperties.COLOR, color));

			for(BlockPos link : linked){
				BlockPos worldLink = pos.add(link);
				BlockState linkState = world.getBlockState(worldLink);
				if(linkState.getBlock() == ESBlocks.redstoneReceiver){
					world.setBlockState(worldLink, linkState.with(ESProperties.COLOR, color));
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
	public void clearLinks(){
		for(BlockPos linkPos : linked){
			TileEntity te = world.getTileEntity(linkPos);
			if(te instanceof RedstoneReceiverTileEntity){
				((RedstoneReceiverTileEntity) te).setSrc(null);
			}
		}
		linked.clear();
		markDirty();
		BlockUtil.sendClientPacketAround(world, pos, new SendLongToClient(CLEAR_PACKET_ID, 0, pos));
	}

	public void buildConnections(){
		//Rebuild the sources list

		if(!world.isRemote){
			builtConnections = true;
			ArrayList<Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Direction>> preSrc = new ArrayList<>(sources.size());
			preSrc.addAll(sources);
			//Wipe old sources
			sources.clear();

			for(Direction checkDir : Direction.values()){
				TileEntity te = world.getTileEntity(pos.offset(checkDir));
				IRedstoneHandler otherHandler;
				if(te != null && (otherHandler = BlockUtil.get(te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, checkDir.getOpposite()))) != null){
					otherHandler.requestSrc(circRef, 0, checkDir.getOpposite(), checkDir);
				}
			}

			//if sources changed, schedule an update
			if(sources.size() != preSrc.size() || !sources.containsAll(preSrc)){
				world.getPendingBlockTicks().scheduleTick(pos, ESBlocks.redstoneTransmitter, RedstoneUtil.DELAY, TickPriority.NORMAL);
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

			sidesToCheck[ref.getRight().getIndex()] = null;
			input = Math.max(input, RedstoneUtil.sanitize(handl.getOutput()));
		}

		//Any input without a circuit input uses vanilla redstone instead
		//Don't check any side with a circuit
		for(Direction dir : sidesToCheck){
			if(dir != null){
				input = Math.max(RedstoneUtil.getRedstoneOnSide(world, pos, dir), input);
			}
		}

		input = RedstoneUtil.sanitize(input);

		if(RedstoneUtil.didChange(output, input)){
			output = input;
			for(BlockPos link : linked){
				TileEntity te = world.getTileEntity(pos.add(link));
				if(te instanceof RedstoneReceiverTileEntity){
					((RedstoneReceiverTileEntity) te).notifyOutputChange();
				}
			}
			markDirty();
		}
	}

	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt = super.getUpdateTag();
		int count = 0;
		for(BlockPos relPos : linked){
			nbt.putLong("link_" + count++, relPos.toLong());
		}
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		output = nbt.getFloat("out");
		int i = 0;
		while(nbt.contains("link_" + i)){
			linked.add(BlockPos.fromLong(nbt.getLong("link_" + i)));
			i++;
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putFloat("out", output);
		int count = 0;
		for(BlockPos relPos : linked){
			nbt.putLong("link_" + count++, relPos.toLong());
		}
		return nbt;
	}

	@Override
	public TileEntity getTE(){
		return this;
	}

	@Override
	public boolean canLink(ILinkTE otherTE){
		return otherTE instanceof RedstoneReceiverTileEntity;
	}

	@Override
	public Set<BlockPos> getLinks(){
		return linked;
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
	public boolean link(ILinkTE endpoint, PlayerEntity player){
		BlockPos linkPos = endpoint.getTE().getPos().subtract(pos);
		if(linked.contains(linkPos)){
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.taken"), player.getUniqueID());
		}else if(linked.size() < getMaxLinks()){
			linked.add(linkPos);
			BlockUtil.sendClientPacketAround(world, pos, new SendLongToClient(LINK_PACKET_ID, linkPos.toLong(), pos));
			getTE().markDirty();
			RedstoneReceiverTileEntity recTe = (RedstoneReceiverTileEntity) endpoint;
			recTe.setSrc(pos.subtract(recTe.getPos()));
			recTe.dye(world.getBlockState(pos).get(ESProperties.COLOR));

			world.neighborChanged(linkPos, ESBlocks.redstoneTransmitter, linkPos);
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.success", getTE().getPos(), endpoint.getTE().getPos()), player.getUniqueID());
			return true;
		}else{
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.full", getMaxLinks()), player.getUniqueID());
		}
		return false;
	}

	@Override
	public void receiveLong(byte identifier, long message, @Nullable ServerPlayerEntity sendingPlayer){
		if(identifier == LINK_PACKET_ID){
			linked.add(BlockPos.fromLong(message));
		}else if(identifier == CLEAR_PACKET_ID){
			linked.clear();
		}
	}

	@Override
	public void remove(){
		super.remove();
		circOpt.invalidate();
	}

	private final LazyOptional<IRedstoneHandler> circOpt = LazyOptional.of(CircuitHandler::new);
	private WeakReference<LazyOptional<IRedstoneHandler>> circRef = new WeakReference<>(circOpt);

	private ArrayList<Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Direction>> sources = new ArrayList<>(1);

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
			world.getPendingBlockTicks().scheduleTick(pos, ESBlocks.redstoneTransmitter, RedstoneUtil.DELAY, TickPriority.HIGH);
		}
	}
}
