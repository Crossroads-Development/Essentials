package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ReaderCircuit extends AbstractCircuit{

	public ReaderCircuit(){
		super("reader_circuit");
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return false;
	}

	@Override
	public boolean getWeakChanges(BlockState state, IWorldReader world, BlockPos pos){
		return true;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor){
		if(world instanceof World){
			neighborChanged(state, (World) world, pos, this, neighbor, false);
		}
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		World world = te.getWorld();
		BlockPos pos = te.getPos();
		Direction back = CircuitTileEntity.Orient.BACK.getFacing(te.getBlockState().get(ESProperties.HORIZ_FACING));
		float output;
		pos = pos.offset(back);
		BlockState state = te.getBlockState();
		Block block = state.getBlock();
		IReadable readable = RedstoneUtil.getReadable(block);
		if(readable != null){
			output = readable.read(world, pos, state);
		}else if(state.hasComparatorInputOverride()){
			output = state.getComparatorInputOverride(world, pos);
		}else if(state.isNormalCube(world, pos)){
			pos = pos.offset(back);
			state = world.getBlockState(pos);
			block = state.getBlock();
			readable = RedstoneUtil.getReadable(block);
			if(readable != null){
				output = readable.read(world, pos, state);
			}else if(state.hasComparatorInputOverride()){
				output = state.getComparatorInputOverride(world, pos);
			}else{
				output = 0;
			}
		}else{
			output = 0;
		}
		return output;
	}

	@Override
	public boolean usesQuartz(){
		return false;
	}
}
