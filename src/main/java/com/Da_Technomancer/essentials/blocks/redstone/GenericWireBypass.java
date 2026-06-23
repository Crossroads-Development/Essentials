package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public abstract class GenericWireBypass extends AbstractTile{

	protected GenericWireBypass(String name){
		super(name);
	}

	/**
	 *
	 * @param fromDirection The direction the wire connection is coming from
	 * @param state State of this block
	 * @return All directions the wire connects TO (not including fromDirection itself)
	 */
	public abstract Direction[] getMappedDirection(Direction fromDirection, BlockState state);

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		//Wire bypasses propagate block updates to make sure any attached circuit can update when a new connection is made/broken
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof WireTileEntity wte){
			//Prevent the repeated updating of the same wire within a gametick
			long worldTime = worldIn.getGameTime();
			if(worldTime == wte.lastUpdateTime){
				return;
			}

			wte.lastUpdateTime = worldTime;

			if(fromPos == null || fromPos.equals(pos)){
				for(Direction dir : Direction.Plane.HORIZONTAL){
					worldIn.neighborChanged(pos.relative(dir), this, pos);
				}
			}else{
				//If possible, only propagate the block update along the connected wire directions, as this is a bypass
				BlockPos updateFromRelative = fromPos.subtract(pos);
				Direction fromDir = Direction.getNearest(updateFromRelative.getX(), updateFromRelative.getY(), updateFromRelative.getZ());
				if(fromDir.getAxis() != Direction.Axis.Y && updateFromRelative.distManhattan(BlockPos.ZERO) == 1){
					for(Direction toDir : getMappedDirection(fromDir, state)){
						worldIn.neighborChanged(pos.relative(toDir), this, pos);
					}
				}
			}
		}

		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		worldIn.neighborChanged(pos, this, pos);
	}
}
