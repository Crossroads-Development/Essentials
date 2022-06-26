package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.TimerCircuitContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TimerCircuitScreen extends CircuitScreen<TimerCircuitContainer>{

	public TimerCircuitScreen(TimerCircuitContainer screenContainer, Inventory inv, Component titleIn){
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, Component.translatable("container.timer_circuit.period"));
		createTextBar(1, 18, 58, Component.translatable("container.timer_circuit.duration"));
	}
}
