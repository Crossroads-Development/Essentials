package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
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
import java.util.HashSet;

public class WireTileEntity extends TileEntity{

	@ObjectHolder(Essentials.MODID + ":wire")
	private static TileEntityType<WireTileEntity> TYPE = null;

	public long lastUpdateTime;
	protected LazyOptional<RedsHandler> redsOptional = LazyOptional.of(this::createRedsHandler);

	protected WireTileEntity(TileEntityType<? extends WireTileEntity> type){
		super(type);
	}

	public WireTileEntity(){
		this(TYPE);
	}

	protected RedsHandler createRedsHandler(){
		return new RedsHandler();
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		redsOptional.invalidate();
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
		if(cap == RedstoneUtil.REDSTONE_CAPABILITY && (side == null || side.getAxis() != Direction.Axis.Y)){
			return (LazyOptional<T>) redsOptional;
		}
		return super.getCapability(cap, side);
	}

	protected class RedsHandler implements IRedstoneHandler{

		@Override
		public float getOutput(){
			return 0;
		}

		@Override
		public void findDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(worldPosition);
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != fromSide){
					TileEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
					IRedstoneHandler handler;
					if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite()))) != null){
						if(handler instanceof RedsHandler){
							((RedsHandler) handler).routeDependents(src, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.findDependents(src, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		//A more efficient routing algorithm that is used in place of the stricter API when going between wires, which can be expected to be well behaved
		protected void routeDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(worldPosition) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != fromSide){
					TileEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
					IRedstoneHandler handler;
					if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite()))) != null){
						if(handler instanceof RedsHandler){
							((RedsHandler) handler).routeDependents(src, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.findDependents(src, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		@Override
		public void requestSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(worldPosition);
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != toSide){
					TileEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
					IRedstoneHandler handler;
					if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite()))) != null){
						if(handler instanceof RedsHandler){
							((RedsHandler) handler).routeSrc(dependency, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.requestSrc(dependency, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		//A more efficient routing algorithm that is used in place of the stricter API when going between wires, which can be expected to be well behaved
		protected void routeSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(worldPosition) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != toSide){
					TileEntity neighbor = level.getBlockEntity(worldPosition.relative(dir));
					IRedstoneHandler handler;
					if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite()))) != null){
						if(handler instanceof RedsHandler){
							((RedsHandler) handler).routeSrc(dependency, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.requestSrc(dependency, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		@Override
		public void addSrc(WeakReference<LazyOptional<IRedstoneHandler>> src, Direction fromSide){

		}

		@Override
		public void addDependent(WeakReference<LazyOptional<IRedstoneHandler>> dependent, Direction toSide){

		}

		@Override
		public void notifyInputChange(WeakReference<LazyOptional<IRedstoneHandler>> src){

		}
	}
}
