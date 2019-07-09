package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.tileentities.WireJunctionTileEntity;
import com.Da_Technomancer.essentials.tileentities.WireTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class WireJunctionCircuit extends AbstractTile{

	public WireJunctionCircuit(){
		super("wire_junction_circuit");
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		//Wire junctions propogate block updates in horizontally to make sure any attached circuit can update when a new connection is made/broken
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof WireTileEntity){
			WireTileEntity wte = (WireTileEntity) te;

			//Prevent the repeated updating of the same wire within a gametick
			long worldTime = worldIn.getGameTime();
			if(worldTime == wte.lastUpdateTime){
				return;
			}

			wte.lastUpdateTime = worldTime;

			if(fromPos == null || fromPos.equals(pos)){
				for(Direction dir : Direction.Plane.HORIZONTAL){
					worldIn.neighborChanged(pos.offset(dir), this, pos);
				}
			}else{
				//If possible, only propogate the block update along the direction it came from- as this is a junction
				Direction dir = Direction.getFacingFromVector(pos.getX() - fromPos.getX(), pos.getY() - fromPos.getY(), pos.getZ() - fromPos.getZ());
				if(dir.getAxis() != Direction.Axis.Y){
					worldIn.neighborChanged(pos.offset(dir), this, pos);
				}
			}
		}

		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn){
		return new WireJunctionTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		worldIn.neighborChanged(pos, this, pos);
	}
}
