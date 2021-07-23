package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class DelayCircuitContainer extends CircuitContainer{

	@ObjectHolder("delay_circuit")
	private static MenuType<DelayCircuitContainer> TYPE = null;

	public DelayCircuitContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		super(TYPE, id, playerInventory, data);
	}

	@Override
	public int inputBars(){
		return 1;
	}
}
