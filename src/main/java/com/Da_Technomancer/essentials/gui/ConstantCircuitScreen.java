package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.ConstantCircuitContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ConstantCircuitScreen extends CircuitScreen<ConstantCircuitContainer>{

	public ConstantCircuitScreen(ConstantCircuitContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, Component.translatable("container.cons_circuit.bar"));
	}
}
