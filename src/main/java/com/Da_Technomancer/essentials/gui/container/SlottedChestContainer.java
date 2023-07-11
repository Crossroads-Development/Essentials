package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.BlockMenuContainer;
import com.Da_Technomancer.essentials.blocks.SlottedChestTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ObjectHolder;

public class SlottedChestContainer extends BlockMenuContainer<SlottedChestTileEntity>{

	@ObjectHolder(registryName = "menu", value = Essentials.MODID + ":slotted_chest")
	private static MenuType<SlottedChestContainer> TYPE = null;

	public SlottedChestContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		super(TYPE, id, playerInventory, data);
		for(int i = 0; i < te.lockedInv.length; i++){
			//Populate the client-side lock info from the byte buffer
			te.lockedInv[i] = data.readItem();
		}
	}

	@Override
	protected int[] getInvStart(){
		return new int[] {8, 139};
	}

	@Override
	protected void addSlots(){
		int numRows = te.lockedInv.length / 9;

		for(int j = 0; j < numRows; ++j){
			for(int k = 0; k < 9; ++k){
				addSlot(new SlottedChestStrictSlot(te, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}
	}

	@Override
	protected int slotCount(){
		return te.lockedInv.length;
	}

	public ItemStack getFilterInSlot(int slot){
		return te.lockedInv[slot];
	}

	@Override
	protected boolean moveItemStackTo(ItemStack movedStack, int slotIndexStart, int slotIndexEnd, boolean toPlayerInv){
		/*
		 * Special handling of shift-clicking items into the chest
		 * Items which are shift-clicked in are NOT allowed into slots with no filter set
		 * Implementation copied from superclass
		 */

		if(toPlayerInv){
			//Use normal behavior
			return super.moveItemStackTo(movedStack, slotIndexStart, slotIndexEnd, toPlayerInv);
		}


		boolean flag = false;
		int i = slotIndexStart;
		if(toPlayerInv){
			i = slotIndexEnd - 1;
		}

		if(movedStack.isStackable()){
			while(!movedStack.isEmpty()){
				if(toPlayerInv){
					if(i < slotIndexStart){
						break;
					}
				}else if(i >= slotIndexEnd){
					break;
				}

				Slot slot = this.slots.get(i);
				ItemStack itemstack = slot.getItem();
				if(!itemstack.isEmpty() && ItemStack.isSameItemSameTags(movedStack, itemstack)){
					int j = itemstack.getCount() + movedStack.getCount();
					int maxSize = Math.min(slot.getMaxStackSize(), movedStack.getMaxStackSize());
					if(j <= maxSize){
						movedStack.setCount(0);
						itemstack.setCount(j);
						slot.setChanged();
						flag = true;
					}else if(itemstack.getCount() < maxSize){
						movedStack.shrink(maxSize - itemstack.getCount());
						itemstack.setCount(maxSize);
						slot.setChanged();
						flag = true;
					}
				}

				if(toPlayerInv){
					--i;
				}else{
					++i;
				}
			}
		}

		if(!movedStack.isEmpty()){
			if(toPlayerInv){
				i = slotIndexEnd - 1;
			}else{
				i = slotIndexStart;
			}

			while(true){
				if(toPlayerInv){
					if(i < slotIndexStart){
						break;
					}
				}else if(i >= slotIndexEnd){
					break;
				}

				Slot slot1 = this.slots.get(i);
				ItemStack itemstack1 = slot1.getItem();
				//Next line was originally: if(itemstack1.isEmpty() && slot1.mayPlace(movedStack)){
				if(itemstack1.isEmpty() && te.canPlaceItem(i, movedStack)){
					if(movedStack.getCount() > slot1.getMaxStackSize()){
						slot1.setByPlayer(movedStack.split(slot1.getMaxStackSize()));
					}else{
						slot1.setByPlayer(movedStack.split(movedStack.getCount()));
					}

					slot1.setChanged();
					flag = true;
					break;
				}

				if(toPlayerInv){
					--i;
				}else{
					++i;
				}
			}
		}

		return flag;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int fromSlot){
		if(fromSlot < slotCount() && !getSlot(fromSlot).hasItem()){
			//Shift-clicked empty slot in TE inventory
			//Clear the filter
			te.lockedInv[fromSlot] = ItemStack.EMPTY;
			te.setChanged();
		}

		return super.quickMoveStack(playerIn, fromSlot);
	}

	private static class SlottedChestStrictSlot extends StrictSlot{

		private final SlottedChestTileEntity ste;

		public SlottedChestStrictSlot(SlottedChestTileEntity te, int index, int x, int y){
			super(te, index, x, y);
			this.ste = te;
		}

		@Override
		public boolean mayPlace(ItemStack stack){
			return ste.canPlaceItemUI(index, stack);
		}
	}
}
