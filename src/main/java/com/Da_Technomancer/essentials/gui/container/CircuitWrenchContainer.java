package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
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
	public boolean canInteractWith(PlayerEntity playerIn){
		return playerIn.getHeldItemOffhand().getItem() == EssentialsItems.circuitWrench || playerIn.getHeldItemMainhand().getItem() == EssentialsItems.circuitWrench;
	}
}
