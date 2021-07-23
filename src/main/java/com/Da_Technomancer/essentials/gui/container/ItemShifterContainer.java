package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class ItemShifterContainer extends Container{

	@ObjectHolder("item_shifter")
	private static ContainerType<ItemShifterContainer> TYPE = null;

	private final IInventory inv;

	public ItemShifterContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		this(id, playerInventory, new Inventory(1));
	}

	public ItemShifterContainer(int id, PlayerInventory playerInventory, IInventory inv){
		super(TYPE, id);
		this.inv = inv;
		addSlot(new Slot(inv, 0, 80, 32));

		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 9; j++){
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++){
			addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean stillValid(Player playerIn){
		return inv.stillValid(playerIn);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int fromSlot){
		ItemStack previous = ItemStack.EMPTY;
		Slot slot = slots.get(fromSlot);

		if(slot != null && slot.hasItem()){
			ItemStack current = slot.getItem();
			previous = current.copy();

			//fromSlot == 0 means TE -> Player, else Player -> TE input slots
			if(fromSlot == 0 ? !moveItemStackTo(current, 1, 37, true) : !moveItemStackTo(current, 0, 1, false)){
				return ItemStack.EMPTY;
			}

			if(current.isEmpty()){
				slot.set(ItemStack.EMPTY);
			}else{
				slot.setChanged();
			}

			if(current.getCount() == previous.getCount()){
				return ItemStack.EMPTY;
			}
			slot.onTake(playerIn, current);
		}

		return previous;
	}
}
