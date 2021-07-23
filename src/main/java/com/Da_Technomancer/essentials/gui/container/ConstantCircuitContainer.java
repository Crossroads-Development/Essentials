package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class ConstantCircuitContainer extends CircuitContainer{

	@ObjectHolder("cons_circuit")
	private static MenuType<ConstantCircuitContainer> TYPE = null;

	public ConstantCircuitContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		super(TYPE, id, playerInventory, data);
	}

	@Override
	public int inputBars(){
		return 1;
	}
}
