package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ObjectHolder;

public class TimerCircuitContainer extends CircuitContainer{

	@ObjectHolder(registryName="menu", value=Essentials.MODID + ":timer_circuit")
	private static MenuType<TimerCircuitContainer> TYPE = null;

	public TimerCircuitContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		super(TYPE, id, playerInventory, data);
	}

	@Override
	public int inputBars(){
		return 2;
	}
}
