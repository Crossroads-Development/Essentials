package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class CircuitWrenchContainer extends Container{

	@ObjectHolder("circuit_wrench")
	private static ContainerType<CircuitWrenchContainer> TYPE = null;

	public CircuitWrenchContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		super(TYPE, id);
	}

	@Override
	public boolean stillValid(Player playerIn){
		return playerIn.getOffhandItem().getItem() == ESItems.circuitWrench || playerIn.getMainHandItem().getItem() == ESItems.circuitWrench;
	}
}
