package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.api.ILinkTE;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

public class RedstoneReceiverTileEntity extends BlockEntity implements ILinkTE{

	private BlockPos src = null;

	@ObjectHolder(registryName="block_entity_type", value=Essentials.MODID + ":redstone_receiver")
	public static BlockEntityType<RedstoneReceiverTileEntity> type = null;

	public RedstoneReceiverTileEntity(BlockPos pos, BlockState state){
		super(type, pos, state);
	}

	@Override
	public void receiveLong(byte identifier, long message, @Nullable ServerPlayer sendingPlayer){
		//No-Op, doesn't create links
	}

	@Override
	public boolean canBeginLinking(){
		return false;
	}

	@Override
	public boolean createLinkSource(ILinkTE endpoint, @Nullable Player player){
		return false;//No-Op, doesn't create links
	}

	@Override
	public void removeLinkSource(BlockPos end){
		//No-op, doesn't create links
	}

	@Override
	public void createLinkEnd(ILinkTE newSrcTE){
		if(src != null){
			//Unlink from the previous source if applicable
			BlockPos worldSrc = worldPosition.offset(src);
			BlockEntity srcTE = level.getBlockEntity(worldSrc);
			if(srcTE instanceof RedstoneTransmitterTileEntity){
				((RedstoneTransmitterTileEntity) srcTE).removeLinkSource(worldPosition.subtract(worldSrc));
			}
		}
		src = newSrcTE == null ? null : newSrcTE.getTE().getBlockPos().subtract(worldPosition);
		if(newSrcTE instanceof RedstoneTransmitterTileEntity){
			//Dye this block to match the source
			BlockState srcState = newSrcTE.getTE().getBlockState();
			level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(ESProperties.COLOR, srcState.getBlock() == ESBlocks.redstoneTransmitter ? srcState.getValue(ESProperties.COLOR) : DyeColor.WHITE));
		}
		setChanged();
		notifyOutputChange();
	}

	@Override
	public void removeLinkEnd(BlockPos src){
		createLinkEnd(null);
	}

	public void dye(DyeColor color){
		if(level.getBlockState(worldPosition).getValue(ESProperties.COLOR) != color){
			level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(ESProperties.COLOR, color));
			if(src != null){
				BlockPos worldSrc = worldPosition.offset(src);
				BlockEntity srcTE = level.getBlockEntity(worldSrc);
				if(srcTE instanceof RedstoneTransmitterTileEntity){
					((RedstoneTransmitterTileEntity) srcTE).dye(color);
				}
			}
		}
	}

	protected void notifyOutputChange(){
		//Notify dependents and/or neighbors that getPower output has changed
		level.updateNeighborsAt(worldPosition, ESBlocks.redstoneReceiver);
		for(int i = 0; i < dependents.size(); i++){
			WeakReference<LazyOptional<IRedstoneHandler>> depend = dependents.get(i);
			LazyOptional<IRedstoneHandler> optional;
			//Validate dependent
			if(depend == null || (optional = depend.get()) == null || !optional.isPresent()){
				dependents.remove(i);
				i--;
				continue;
			}
			//Notify the dependent of a change
			optional.orElseThrow(NullPointerException::new).notifyInputChange(circRef);
		}
	}

	//Rebuilds the list of dependents
	public void buildDependents(){
		dependents.clear();//Wipe the old dependents list

		//Check in all 6 directions because this block outputs in every direction
		for(Direction dir : Direction.values()){
			BlockEntity te = level.getBlockEntity(worldPosition.relative(dir));
			LazyOptional<IRedstoneHandler> otherOpt;
			if(te != null && (otherOpt = te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite())).isPresent()){
				IRedstoneHandler otherHandler = otherOpt.orElseThrow(NullPointerException::new);
				otherHandler.findDependents(circRef, 0, dir.getOpposite(), dir);
			}
		}
	}

	public float getPower(){
		if(src != null){
			BlockEntity te = level.getBlockEntity(worldPosition.offset(src));
			if(te instanceof RedstoneTransmitterTileEntity){
				return ((RedstoneTransmitterTileEntity) te).getOutput();
			}
		}
		return 0;
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		if(nbt.contains("src")){
			src = BlockPos.of(nbt.getLong("src"));
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		if(src != null){
			nbt.putLong("src", src.asLong());
		}
	}

	@Override
	public BlockEntity getTE(){
		return this;
	}

	@Override
	public boolean canLink(ILinkTE otherTE){
		//Receiving only
		return false;
	}

	@Override
	public HashSet<BlockPos> getLinks(){
		return new HashSet<>(1);
	}

	@Override
	public int getMaxLinks(){
		return 0;
	}

	@Override
	public int getRange(){
		return ESConfig.wirelessRange.get();
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		circOpt.invalidate();
	}

	private LazyOptional<IRedstoneHandler> circOpt = LazyOptional.of(CircHandler::new);
	private WeakReference<LazyOptional<IRedstoneHandler>> circRef = new WeakReference<>(circOpt);
	private final ArrayList<WeakReference<LazyOptional<IRedstoneHandler>>> dependents = new ArrayList<>(1);

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
		if(cap == RedstoneUtil.REDSTONE_CAPABILITY){
			return (LazyOptional<T>) circOpt;
		}
		return super.getCapability(cap, side);
	}

	private class CircHandler implements IRedstoneHandler{

		@Override
		public float getOutput(){
			return getPower();
		}

		@Override
		public void findDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide){
			//No-Op
		}

		@Override
		public void requestSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide){
			LazyOptional<IRedstoneHandler> depenOption;
			if((depenOption = dependency.get()) != null && depenOption.isPresent()){
				IRedstoneHandler depHandler = depenOption.orElseThrow(NullPointerException::new);
				depHandler.addSrc(circRef, nominalSide);
				if(!dependents.contains(dependency)){
					dependents.add(dependency);
				}
			}
		}

		@Override
		public void addSrc(WeakReference<LazyOptional<IRedstoneHandler>> src, Direction fromSide){
			//No-Op
		}

		@Override
		public void addDependent(WeakReference<LazyOptional<IRedstoneHandler>> dependent, Direction toSide){
			if(!dependents.contains(dependent)){
				dependents.add(dependent);
			}
		}

		@Override
		public void notifyInputChange(WeakReference<LazyOptional<IRedstoneHandler>> src){
			//No-Op
		}
	}
}
