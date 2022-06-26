package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Function;

public class InterfaceCircuit extends GenericACircuit{

	public InterfaceCircuit(String name, Function<Float, Float> function, boolean usesQuartz){
		super(name, function, usesQuartz);
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		if(te instanceof InterfaceCircuitTileEntity ite && ite.externalInput != null){
			//Used by computercraft integration
			//Normal output is overridden by the externalInput field if applicable.
			return ite.externalInput;
		}
		return super.getOutput(in0, in1, in2, te);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new InterfaceCircuitTileEntity(pos, state);
	}
}
