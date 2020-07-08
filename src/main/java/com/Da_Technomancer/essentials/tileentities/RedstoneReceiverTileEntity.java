package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

@ObjectHolder(Essentials.MODID)
public class RedstoneReceiverTileEntity extends TileEntity implements ILinkTE{

	private BlockPos src = null;

	@ObjectHolder("redstone_receiver")
	private static TileEntityType<RedstoneReceiverTileEntity> type = null;

	public RedstoneReceiverTileEntity(){
		super(type);
	}

	@Override
	public void receiveLong(byte identifier, long message, @Nullable ServerPlayerEntity sendingPlayer){
		//No-Op, doesn't create links
	}

	@Override
	public boolean canBeginLinking(){
		return false;
	}

	//Used for bi-directional linking
	protected void setSrc(BlockPos srcIn){
		src = srcIn;
		markDirty();
		notifyOutputChange();
	}

	public void dye(DyeColor color){
		if(world.getBlockState(pos).get(ESProperties.COLOR) != color){
			world.setBlockState(pos, world.getBlockState(pos).with(ESProperties.COLOR, color));
			if(src != null){
				BlockPos worldSrc = pos.add(src);
				TileEntity srcTE = world.getTileEntity(worldSrc);
				if(srcTE instanceof RedstoneTransmitterTileEntity){
					((RedstoneTransmitterTileEntity) srcTE).dye(color);
				}
			}
		}
	}

	protected void notifyOutputChange(){
		//Notify dependents and/or neighbors that getPower output has changed
		world.notifyNeighborsOfStateChange(pos, ESBlocks.redstoneReceiver);
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
			TileEntity te = world.getTileEntity(pos.offset(dir));
			LazyOptional<IRedstoneHandler> otherOpt;
			if(te != null && (otherOpt = te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite())).isPresent()){
				IRedstoneHandler otherHandler = otherOpt.orElseThrow(NullPointerException::new);
				otherHandler.findDependents(circRef, 0, dir.getOpposite(), dir);
			}
		}
	}

	public float getPower(){
		if(src != null){
			TileEntity te = world.getTileEntity(pos.add(src));
			if(te instanceof RedstoneTransmitterTileEntity){
				return ((RedstoneTransmitterTileEntity) te).getOutput();
			}
		}
		return 0;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		if(nbt.contains("src")){
			src = BlockPos.fromLong(nbt.getLong("src"));
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		if(src != null){
			nbt.putLong("src", src.toLong());
		}
		return nbt;
	}

	@Override
	public TileEntity getTE(){
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
	public void remove(){
		super.remove();
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
