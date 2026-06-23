package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;

public abstract class GenericWireBypassTileEntity extends WireTileEntity{

	protected GenericWireBypassTileEntity(BlockEntityType<? extends WireTileEntity> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}

	@Override
	protected RedsHandler createRedsHandler(){
		//Use a special handler that routes only in certain directions
		return new BypassRedsHandler();
	}

	protected abstract GenericWireBypass getBlock();

	protected class BypassRedsHandler extends RedsHandler{

		@Override
		public void findDependents(IRedstoneHandler src, int dist, Direction fromSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(worldPosition);

			for(Direction dir : getBlock().getMappedDirection(fromSide, getBlockState())){
				IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), fromSide);
				if(handler != null){
					if(handler instanceof RedsHandler otherRedsHandler){
						otherRedsHandler.routeDependents(src, dist, fromSide, nominalSide, visited);
					}else{
						handler.findDependents(src, dist, fromSide, nominalSide);
					}
				}
			}
		}

		@Override
		protected void routeDependents(IRedstoneHandler src, int dist, Direction fromSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(worldPosition) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}

			for(Direction dir : getBlock().getMappedDirection(fromSide, getBlockState())){
				IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), fromSide);
				if(handler != null){
					if(handler instanceof RedsHandler otherRedsHandler){
						otherRedsHandler.routeDependents(src, dist, fromSide, nominalSide, visited);
					}else{
						handler.findDependents(src, dist, fromSide, nominalSide);
					}
				}
			}
		}

		@Override
		public void requestSrc(IRedstoneHandler dependency, int dist, Direction toSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(worldPosition);
			for(Direction dir : getBlock().getMappedDirection(toSide, getBlockState())){
				IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), toSide);
				if(handler != null){
					if(handler instanceof RedsHandler otherRedsHandler){
						otherRedsHandler.routeSrc(dependency, dist, toSide, nominalSide, visited);
					}else{
						handler.requestSrc(dependency, dist, toSide, nominalSide);
					}
				}
			}
		}

		@Override
		protected void routeSrc(IRedstoneHandler dependency, int dist, Direction toSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(worldPosition) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}

			for(Direction dir : getBlock().getMappedDirection(toSide, getBlockState())){
				IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), toSide);
				if(handler != null){
					if(handler instanceof RedsHandler otherRedsHandler){
						otherRedsHandler.routeSrc(dependency, dist, toSide, nominalSide, visited);
					}else{
						handler.requestSrc(dependency, dist, toSide, nominalSide);
					}
				}
			}
		}
	}
}
