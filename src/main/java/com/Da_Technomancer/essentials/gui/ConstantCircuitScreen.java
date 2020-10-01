package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.ConstantCircuitContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ConstantCircuitScreen extends CircuitScreen<ConstantCircuitContainer>{

	public ConstantCircuitScreen(ConstantCircuitContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();

		createTextBar(0, 18, 28, new TranslationTextComponent("container.cons_circuit.bar"));
	}
}
