package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.PulseCircuitContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class PulseCircuitScreen extends CircuitScreen<PulseCircuitContainer>{

	public PulseCircuitScreen(PulseCircuitContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, new TranslatableComponent("container.pulse_circuit.duration"));
	}
}
