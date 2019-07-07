package com.Da_Technomancer.essentials.blocks.redstone;

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

public class WireCircuit extends AbstractTile{

	public WireCircuit(){
		super("wire_circuit");
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		//Wires propogate block updates in all horizontal directions to make sure any attached circuit can update when a new connection is made/broken
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof WireTileEntity){
			WireTileEntity wte = (WireTileEntity) te;

			//Prevent the repeated updating of the same wire within a gametick
			long worldTime = worldIn.getGameTime();
			if(worldTime == wte.lastUpdateTime){
				return;
			}

			wte.lastUpdateTime = worldTime;

			for(Direction dir : Direction.Plane.HORIZONTAL){
				worldIn.neighborChanged(pos.offset(dir), this, pos);
			}
		}

		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn){
		return new WireTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		worldIn.neighborChanged(pos, this, pos);
	}
}
