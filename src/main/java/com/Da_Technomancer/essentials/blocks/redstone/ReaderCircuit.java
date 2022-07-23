package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.redstone.IReadable;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.api.ESProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

public class ReaderCircuit extends AbstractCircuit{

	public ReaderCircuit(){
		super("reader_circuit");
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return false;
	}

	@Override
	public boolean getWeakChanges(BlockState state, LevelReader world, BlockPos pos){
		return true;
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor){
		if(world instanceof Level){
			neighborChanged(state, (Level) world, pos, this, neighbor, false);
		}
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		Level world = te.getLevel();
		Direction back = CircuitTileEntity.Orient.BACK.getFacing(te.getBlockState().getValue(ESProperties.HORIZ_FACING));
		BlockPos readPos = te.getBlockPos().relative(back);
		float output;
		BlockState state = world.getBlockState(readPos);
		Block block = state.getBlock();
		IReadable readable = RedstoneUtil.getReadable(block);
		if(readable != null){
			output = readable.read(world, readPos, state);
		}else if(state.hasAnalogOutputSignal()){
			output = state.getAnalogOutputSignal(world, readPos);
		}else if(state.isRedstoneConductor(world, readPos)){
			readPos = readPos.relative(back);
			state = world.getBlockState(readPos);
			block = state.getBlock();
			readable = RedstoneUtil.getReadable(block);
			if(readable != null){
				output = readable.read(world, readPos, state);
			}else if(state.hasAnalogOutputSignal()){
				output = state.getAnalogOutputSignal(world, readPos);
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
