package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;

public class ConsCircuit extends AbstractCircuit{

	public ConsCircuit(){
		super("cons_circuit");
	}

	@Override
	public boolean useInput(int index){
		return false;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		return 5;//TODO
	}
}
