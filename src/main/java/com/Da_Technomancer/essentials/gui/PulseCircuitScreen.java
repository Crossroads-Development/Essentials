package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.PulseCircuitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class PulseCircuitScreen extends CircuitScreen<PulseCircuitContainer>{

	public PulseCircuitScreen(PulseCircuitContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, new TranslationTextComponent("container.pulse_circuit.duration"));
	}
}
