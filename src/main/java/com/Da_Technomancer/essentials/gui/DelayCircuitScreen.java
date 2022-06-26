package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.DelayCircuitContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DelayCircuitScreen extends CircuitScreen<DelayCircuitContainer>{

	public DelayCircuitScreen(DelayCircuitContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, Component.translatable("container.delay_circuit.delay"));
	}
}
