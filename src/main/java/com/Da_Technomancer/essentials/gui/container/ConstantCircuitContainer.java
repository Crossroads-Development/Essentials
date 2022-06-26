package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.ObjectHolder;

public class ConstantCircuitContainer extends CircuitContainer{

	@ObjectHolder(registryName="menu", value=Essentials.MODID + ":cons_circuit")
	private static MenuType<ConstantCircuitContainer> TYPE = null;

	public ConstantCircuitContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		super(TYPE, id, playerInventory, data);
	}

	@Override
	public int inputBars(){
		return 1;
	}
}
