package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.tileentities.SlottedChestTileEntity;
import com.google.common.collect.Sets;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.Set;

@ObjectHolder(Essentials.MODID)
public class SlottedChestContainer extends AbstractContainerMenu{

	@ObjectHolder("slotted_chest")
	private static MenuType<SlottedChestContainer> TYPE = null;

	public final SlottedChestTileEntity.SlottedInv inv;
	/**
	 * Holds the locked filter for the slotted chest
	 * On the virtual server side, this instance is shared with the TE, and changes will write back and be saved
	 * On the virtual client, changes will not write back
	 */
	public final ItemStack[] filter;

	/**
	 * This is a slightly hacky way to make the Container and TE share filter instance without messy constructors. Never use this for anything outside what it was made for
	 */
	@Nullable
	private static ItemStack[] filtTrans;

	public SlottedChestContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		//the new ItemStack[54] is full of null entries, so in the SlottedChestTileEntity.Inventory constructor all null entries are set to ItemStack.EMPTY
		this(id, playerInventory, new SlottedChestTileEntity.SlottedInv(new ItemStack[54], filtTrans = decodeBuffer(data), null), filtTrans);
	}

	private static ItemStack[] decodeBuffer(FriendlyByteBuf buf){
		if(buf == null){
			Essentials.logger.warn("Received empty data for SlottedChest! This is a bug!");
			ItemStack[] filter = new ItemStack[54];
			for(int i = 0; i < 54; i++){
				filter[i] = ItemStack.EMPTY;
			}
			return filter;
		}
		ItemStack[] filter = new ItemStack[54];
		for(int i = 0; i < 54; i++){
			filter[i] = buf.readItem();
		}
		return filter;
	}

	public SlottedChestContainer(int id, Inventory playerInventory, SlottedChestTileEntity.SlottedInv inv, ItemStack[] filter){
		super(TYPE, id);
		this.filter = filter;
		this.inv = inv;
		int numRows = inv.getContainerSize() / 9;
		int i = (numRows - 4) * 18;

		for(int j = 0; j < numRows; ++j){
			for(int k = 0; k < 9; ++k){
				addSlot(new Slot(inv, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}

		for(int l = 0; l < 3; ++l){
			for(int j1 = 0; j1 < 9; ++j1){
				addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
			}
		}

		for(int i1 = 0; i1 < 9; ++i1){
			addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
		}
	}

	@Override
	public boolean stillValid(Player playerIn){
		return inv.stillValid(playerIn);
	}

	@Override
	protected void resetQuickCraft(){
		super.resetQuickCraft();
		dragEvent = 0;
		dragSlots.clear();
	}

	private int dragEvent;
	private final Set<Slot> dragSlots = Sets.newHashSet();
	private int dragMode = -1;

	/**
	 * This abomination of a method is copied from the minecraft source code.
	 * It is slightly modified to set filters where necessary (and sometimes even when not, because I can't be bothered to reverse engineer this thing).
	 */
	private void doClick(int slotId, int dragType, ClickType clickTypeIn, Player player){
//		ItemStack itemstack = ItemStack.EMPTY;
		Inventory inventoryplayer = player.getInventory();

		if(clickTypeIn == ClickType.QUICK_CRAFT){
			int i = this.dragEvent;
			dragEvent = getQuickcraftHeader(dragType);

			if((i != 1 || this.dragEvent != 2) && i != this.dragEvent){
				resetQuickCraft();
			}else if(getCarried().isEmpty()){
				resetQuickCraft();
			}else if(this.dragEvent == 0){
				dragMode = getQuickcraftType(dragType);

				if(isValidQuickcraftType(dragMode, player)){
					dragEvent = 1;
					dragSlots.clear();
				}else{
					resetQuickCraft();
				}
			}else if(this.dragEvent == 1){
				Slot slot = slots.get(slotId);
				ItemStack itemstack1 = getCarried();

				if(slot != null && canAddItemToSlotLocked(slot, itemstack1, true) && slot.mayPlace(itemstack1) && (this.dragMode == 2 || itemstack1.getCount() > this.dragSlots.size()) && this.canDragTo(slot)){
					dragSlots.add(slot);
				}
			}else if(dragEvent == 2){
				if(!dragSlots.isEmpty()){
					ItemStack itemstack5 = getCarried().copy();
					int l = getCarried().getCount();

					for(Slot slot1 : dragSlots){
						ItemStack itemstack2 = getCarried();

						if(slot1 != null && canAddItemToSlotLocked(slot1, itemstack2, true) && slot1.mayPlace(itemstack2) && (this.dragMode == 2 || itemstack2.getCount() >= dragSlots.size()) && canDragTo(slot1)){
							ItemStack itemstack3 = itemstack5.copy();
							int j = slot1.hasItem() ? slot1.getItem().getCount() : 0;
							getQuickCraftSlotCount(dragSlots, dragMode, itemstack3, j);
							int k = Math.min(itemstack3.getMaxStackSize(), slot1.getMaxStackSize(itemstack3));

							if(itemstack3.getCount() > k){
								itemstack3.setCount(k);
							}

							l -= itemstack3.getCount() - j;
							slot1.set(itemstack3);
							if(slot1.container instanceof SlottedChestTileEntity.SlottedInv && filter[slot1.getSlotIndex()].isEmpty()){
								filter[slot1.getSlotIndex()] = slot1.getItem().copy();
								filter[slot1.getSlotIndex()].setCount(1);
								inv.filterChanged();
							}
						}
					}

					itemstack5.setCount(l);
					setCarried(itemstack5);
				}

				resetQuickCraft();
			}else{
				resetQuickCraft();
			}
		}else if(this.dragEvent != 0){
			resetQuickCraft();
		}else if((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE) && (dragType == 0 || dragType == 1)){
			if(slotId == -999){
				if(!getCarried().isEmpty()){
					if(dragType == 0){
						player.drop(getCarried(), true);
						setCarried(ItemStack.EMPTY);
					}

					if(dragType == 1){
						player.drop(getCarried().split(1), true);
					}
				}
			}else if(clickTypeIn == ClickType.QUICK_MOVE){
				if(slotId < 0){
					return;
				}

				Slot slot6 = slots.get(slotId);
				//Clear filter on shift left empty click
				if(slot6.container instanceof SlottedChestTileEntity.SlottedInv && !slot6.hasItem()){
					filter[slot6.getSlotIndex()] = ItemStack.EMPTY;
					inv.filterChanged();
				}

				if(slot6.mayPickup(player)){
					ItemStack itemstack10 = quickMoveStack(player, slotId);

//					if(!itemstack10.isEmpty()){
//						itemstack = itemstack10.copy();
//					}
				}
			}else{
				if(slotId < 0){
					return;
				}

				Slot slot7 = slots.get(slotId);

				if(slot7 != null){
					ItemStack itemstack11 = slot7.getItem();
					ItemStack itemstack13 = getCarried();

//					if(!itemstack11.isEmpty()){
//						itemstack = itemstack11.copy();
//					}

					if(itemstack11.isEmpty()){
						if(!itemstack13.isEmpty() && slot7.mayPlace(itemstack13) && canAddItemToSlotLocked(slot7, itemstack13, false)){
							int l2 = dragType == 0 ? itemstack13.getCount() : 1;

							if(l2 > slot7.getMaxStackSize(itemstack13)){
								l2 = slot7.getMaxStackSize(itemstack13);
							}

							slot7.set(itemstack13.split(l2));
							if(slot7.container instanceof SlottedChestTileEntity.SlottedInv && filter[slot7.getSlotIndex()].isEmpty()){
								filter[slot7.getSlotIndex()] = slot7.getItem().copy();
								filter[slot7.getSlotIndex()].setCount(1);
								inv.filterChanged();
							}
						}
					}else if(slot7.mayPickup(player)){
						if(itemstack13.isEmpty()){
							if(itemstack11.isEmpty()){
								slot7.set(ItemStack.EMPTY);
								setCarried(ItemStack.EMPTY);
							}else{
								int k2 = dragType == 0 ? itemstack11.getCount() : (itemstack11.getCount() + 1) / 2;
								setCarried(slot7.remove(k2));

								if(itemstack11.isEmpty()){
									slot7.set(ItemStack.EMPTY);
								}

								slot7.onTake(player, getCarried());
							}
						}else if(slot7.mayPlace(itemstack13)){
							if(itemstack11.getItem() == itemstack13.getItem() && ItemStack.tagMatches(itemstack11, itemstack13)){
								int j2 = dragType == 0 ? itemstack13.getCount() : 1;

								if(j2 > slot7.getMaxStackSize(itemstack13) - itemstack11.getCount()){
									j2 = slot7.getMaxStackSize(itemstack13) - itemstack11.getCount();
								}

								if(j2 > itemstack13.getMaxStackSize() - itemstack11.getCount()){
									j2 = itemstack13.getMaxStackSize() - itemstack11.getCount();
								}

								itemstack13.shrink(j2);
								itemstack11.grow(j2);
							}else if(itemstack13.getCount() <= slot7.getMaxStackSize(itemstack13)){
								slot7.set(itemstack13);
								if(slot7.container instanceof SlottedChestTileEntity.SlottedInv && filter[slot7.getSlotIndex()].isEmpty()){
									filter[slot7.getSlotIndex()] = slot7.getItem().copy();
									filter[slot7.getSlotIndex()].setCount(1);
									inv.filterChanged();
								}
								setCarried(itemstack11);
							}
						}else if(itemstack11.getItem() == itemstack13.getItem() && itemstack13.getMaxStackSize() > 1 && ItemStack.tagMatches(itemstack11, itemstack13) && !itemstack11.isEmpty()){
							int i2 = itemstack11.getCount();

							if(i2 + itemstack13.getCount() <= itemstack13.getMaxStackSize()){
								itemstack13.grow(i2);
								itemstack11 = slot7.remove(i2);

								if(itemstack11.isEmpty()){
									slot7.set(ItemStack.EMPTY);
								}

								slot7.onTake(player, getCarried());
							}
						}
					}

					slot7.setChanged();
				}
			}
		}else if(clickTypeIn == ClickType.SWAP && dragType >= 0 && dragType < 9){
			Slot slot5 = this.slots.get(slotId);
			ItemStack itemstack9 = inventoryplayer.getItem(dragType);
			ItemStack itemstack12 = slot5.getItem();

			if(!itemstack9.isEmpty() || !itemstack12.isEmpty()){
				if(itemstack9.isEmpty()){
					if(slot5.mayPickup(player)){
						inventoryplayer.setItem(dragType, itemstack12);
						slot5.set(ItemStack.EMPTY);
						slot5.onTake(player, itemstack12);
					}
				}else if(itemstack12.isEmpty()){
					if(slot5.mayPlace(itemstack9)){
						int k1 = slot5.getMaxStackSize(itemstack9);

						if(itemstack9.getCount() > k1){
							slot5.set(itemstack9.split(k1));
							if(slot5.container instanceof SlottedChestTileEntity.SlottedInv && filter[slot5.getSlotIndex()].isEmpty()){
								filter[slot5.getSlotIndex()] = slot5.getItem().copy();
								filter[slot5.getSlotIndex()].setCount(1);
								inv.filterChanged();
							}
						}else{
							slot5.set(itemstack9);
							if(slot5.container instanceof SlottedChestTileEntity.SlottedInv && filter[slot5.getSlotIndex()].isEmpty()){
								filter[slot5.getSlotIndex()] = slot5.getItem().copy();
								filter[slot5.getSlotIndex()].setCount(1);
								inv.filterChanged();
							}
							inventoryplayer.setItem(dragType, ItemStack.EMPTY);
						}
					}
				}else if(slot5.mayPickup(player) && slot5.mayPlace(itemstack9)){
					int l1 = slot5.getMaxStackSize(itemstack9);

					if(itemstack9.getCount() > l1){
						slot5.set(itemstack9.split(l1));
						slot5.onTake(player, itemstack12);
						if(slot5.container instanceof SlottedChestTileEntity.SlottedInv && filter[slot5.getSlotIndex()].isEmpty()){
							filter[slot5.getSlotIndex()] = slot5.getItem().copy();
							filter[slot5.getSlotIndex()].setCount(1);
							inv.filterChanged();
						}

						if(!inventoryplayer.add(itemstack12)){
							player.drop(itemstack12, true);
						}
					}else{
						slot5.set(itemstack9);
						if(slot5.container instanceof SlottedChestTileEntity.SlottedInv && filter[slot5.getSlotIndex()].isEmpty()){
							filter[slot5.getSlotIndex()] = slot5.getItem().copy();
							filter[slot5.getSlotIndex()].setCount(1);
							inv.filterChanged();
						}
						inventoryplayer.setItem(dragType, itemstack12);
						slot5.onTake(player, itemstack12);
					}
				}
			}
		}else if(clickTypeIn == ClickType.CLONE && player.isCreative() && getCarried().isEmpty() && slotId >= 0){
			Slot slot4 = this.slots.get(slotId);

			if(slot4 != null && slot4.hasItem()){
				ItemStack itemstack8 = slot4.getItem().copy();
				itemstack8.setCount(itemstack8.getMaxStackSize());
				setCarried(itemstack8);
			}
		}else if(clickTypeIn == ClickType.THROW && getCarried().isEmpty() && slotId >= 0){
			Slot slot3 = this.slots.get(slotId);

			if(slot3 != null && slot3.hasItem() && slot3.mayPickup(player)){
				ItemStack itemstack7 = slot3.remove(dragType == 0 ? 1 : slot3.getItem().getCount());
				slot3.onTake(player, itemstack7);
				player.drop(itemstack7, true);
			}
		}else if(clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0){
			Slot slot2 = this.slots.get(slotId);
			ItemStack itemstack6 = getCarried();

			if(!itemstack6.isEmpty() && (slot2 == null || !slot2.hasItem() || !slot2.mayPickup(player))){
				int i1 = dragType == 0 ? 0 : this.slots.size() - 1;
				int j1 = dragType == 0 ? 1 : -1;

				for(int i3 = 0; i3 < 2; ++i3){
					for(int j3 = i1; j3 >= 0 && j3 < this.slots.size() && itemstack6.getCount() < itemstack6.getMaxStackSize(); j3 += j1){
						Slot slot8 = this.slots.get(j3);

						if(slot8.hasItem() && canAddItemToSlotLocked(slot8, itemstack6, true) && slot8.mayPickup(player) && this.canTakeItemForPickAll(itemstack6, slot8)){
							ItemStack itemstack14 = slot8.getItem();

							if(i3 != 0 || itemstack14.getCount() != itemstack14.getMaxStackSize()){
								int k3 = Math.min(itemstack6.getMaxStackSize() - itemstack6.getCount(), itemstack14.getCount());
								ItemStack itemstack4 = slot8.remove(k3);
								itemstack6.grow(k3);

								if(itemstack4.isEmpty()){
									slot8.set(ItemStack.EMPTY);
								}

								slot8.onTake(player, itemstack4);
							}
						}
					}
				}
			}

			broadcastChanges();
		}

		return;
	}

	//Copied from the vanilla method, so we can use our doClick method (the vanilla one is private)
	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player){
		try{
			doClick(slotId, dragType, clickTypeIn, player);
		}catch(Exception exception){
			CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
			CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
			crashreportcategory.setDetail("Menu Type", () -> {
				return getType() != null ? Registry.MENU.getKey(getType()).toString() : "<no type>";
			});
			crashreportcategory.setDetail("Menu Class", () -> {
				return getClass().getCanonicalName();
			});
			crashreportcategory.setDetail("Slot Count", this.slots.size());
			crashreportcategory.setDetail("Slot", slotId);
			crashreportcategory.setDetail("Button", dragType);
			crashreportcategory.setDetail("Type", dragType);
			throw new ReportedException(crashreport);
		}
	}

	/** Take a stack from the specified inventory slot.
	 * Also this version tries to shift click it out if it was in the chest inventory
	 * for some reason I can't remember.
	 */
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index){
		ItemStack outStack = ItemStack.EMPTY;
		Slot slot = slots.get(index);

		if(slot != null && !slot.getItem().isEmpty()){
			ItemStack stackInSlot = slot.getItem();
			outStack = stackInSlot.copy();

			if(index < 54){
				if(!moveItemStackTo(stackInSlot, 54, slots.size(), true)){
					return ItemStack.EMPTY;
				}
			}else if(!moveItemStackTo(stackInSlot, 0, 54, false)){
				return ItemStack.EMPTY;
			}

			slot.setChanged();
		}

		return outStack;
	}

	/** Shift click-transfers an item.
	 * Modified to respect isItemValidForSlot
	 */
	@Override
	protected boolean moveItemStackTo(ItemStack toMerge, int startIndex, int endIndex, boolean chestToPlayer){
		boolean flag = false;
		int i = startIndex;

		if(chestToPlayer){
			i = endIndex - 1;
		}

		if(toMerge.isStackable()){
			while(!toMerge.isEmpty() && (!chestToPlayer && i < endIndex || chestToPlayer && i >= startIndex)){
				Slot slot = slots.get(i);
				ItemStack currentSlotStack = slot.getItem();

				if(!(currentSlotStack.isEmpty() && (chestToPlayer || filter[i].isEmpty())) && (BlockUtil.sameItem(currentSlotStack, toMerge) || (!chestToPlayer && BlockUtil.sameItem(toMerge, filter[i])))){
					int totalCount = currentSlotStack.getCount() + toMerge.getCount();
					if(currentSlotStack.isEmpty()){
						slot.set(filter[i].copy());
						currentSlotStack = slot.getItem();
						currentSlotStack.setCount(0);
					}
					if(totalCount <= toMerge.getMaxStackSize()){
						toMerge.setCount(0);
						currentSlotStack.setCount(totalCount);
						slot.setChanged();
						flag = true;
					}else if(currentSlotStack.getCount() < toMerge.getMaxStackSize()){
						toMerge.shrink(toMerge.getMaxStackSize() - currentSlotStack.getCount());
						currentSlotStack.setCount(toMerge.getMaxStackSize());
						slot.setChanged();
						flag = true;
					}
				}

				if(chestToPlayer){
					--i;
				}else{
					++i;
				}
			}
		}

		if(!toMerge.isEmpty() && chestToPlayer){
			i = endIndex - 1;

			while(i >= startIndex){
				Slot slot = slots.get(i);
				ItemStack currentSlotStack = slot.getItem();

				// Make sure to respect isItemValid in the slot.
				if(currentSlotStack.isEmpty() && slot.mayPlace(toMerge)){
					slot.set(toMerge.copy());
					slot.setChanged();
					toMerge.setCount(0);
					flag = true;
					break;
				}

				--i;
			}
		}

		return flag;
	}

	private boolean canAddItemToSlotLocked(@Nullable Slot slotIn, ItemStack stack, boolean stackSizeMatters){
		if(slotIn != null && slotIn.container instanceof SlottedChestTileEntity.SlottedInv){
			return (filter[slotIn.getSlotIndex()].isEmpty() || BlockUtil.sameItem(stack, filter[slotIn.getSlotIndex()])) && (slotIn.getItem().getCount() + (stackSizeMatters ? 0 : stack.getCount()) <= stack.getMaxStackSize());
		}
		boolean flag = slotIn == null || !slotIn.hasItem();
		return !flag && BlockUtil.sameItem(stack, slotIn.getItem()) ? slotIn.getItem().getCount() + (stackSizeMatters ? 0 : stack.getCount()) <= stack.getMaxStackSize() : flag;
	}
}
