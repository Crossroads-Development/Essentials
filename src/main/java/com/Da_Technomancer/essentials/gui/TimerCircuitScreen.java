package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.TimerCircuitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TimerCircuitScreen extends CircuitScreen<TimerCircuitContainer>{

	public TimerCircuitScreen(TimerCircuitContainer screenContainer, PlayerInventory inv, ITextComponent titleIn){
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, new TranslationTextComponent("container.timer_circuit.period"));
		createTextBar(1, 18, 58, new TranslationTextComponent("container.timer_circuit.duration"));
	}
}
