package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;

public class InterfaceCircuit extends AbstractCircuit{

	public InterfaceCircuit(){
		super("interface_circuit");
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or == CircuitTileEntity.Orient.BACK;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		return in1;
	}
}
