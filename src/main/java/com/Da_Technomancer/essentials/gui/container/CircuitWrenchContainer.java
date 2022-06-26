package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ObjectHolder;

public class CircuitWrenchContainer extends AbstractContainerMenu{

	@ObjectHolder(registryName="menu", value=Essentials.MODID + ":circuit_wrench")
	private static MenuType<CircuitWrenchContainer> TYPE = null;

	public CircuitWrenchContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		super(TYPE, id);
	}

	@Override
	public boolean stillValid(Player playerIn){
		return playerIn.getOffhandItem().getItem() == ESItems.circuitWrench || playerIn.getMainHandItem().getItem() == ESItems.circuitWrench;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int fromSlot){
		return ItemStack.EMPTY;//No-op
	}
}
