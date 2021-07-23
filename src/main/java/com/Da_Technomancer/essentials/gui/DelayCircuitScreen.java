package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.DelayCircuitContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class DelayCircuitScreen extends CircuitScreen<DelayCircuitContainer>{

	public DelayCircuitScreen(DelayCircuitContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, new TranslatableComponent("container.delay_circuit.delay"));
	}
}
