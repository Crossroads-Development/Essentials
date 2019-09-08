package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.ItemShifterContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ObjectHolder(Essentials.MODID)
public class ItemShifterTileEntity extends AbstractShifterTileEntity implements IInventory{

	@ObjectHolder("item_shifter")
	private static TileEntityType<ItemShifterTileEntity> TYPE = null;

	private ItemStack inventory = ItemStack.EMPTY;

	public ItemShifterTileEntity(){
		super(TYPE);
	}

	@Override
	public void tick(){
		if(world.isRemote){
			return;
		}

		if(endPos == null){
			refreshCache();
		}

		if(inventory.isEmpty()){
			return;
		}

		inventory = ejectItem(world, endPos, getFacing(), inventory);
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);

		if(!inventory.isEmpty()){
			nbt.put("inv", inventory.write(new CompoundNBT()));
		}
		return nbt;
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);

		if(nbt.contains("inv")){
			inventory = ItemStack.read(nbt.getCompound("inv"));
		}
	}

	@Override
	public void remove(){
		super.remove();
		invOptional.invalidate();
	}

	private LazyOptional<IItemHandler> invOptional = LazyOptional.of(InventoryHandler::new);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (LazyOptional<T>) invOptional;
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.item_shifter");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player){
		return new ItemShifterContainer(id, playerInventory, this);
	}

	private class InventoryHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			return slot == 0 ? inventory : ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot != 0 || stack.isEmpty() || !inventory.isEmpty() && (!inventory.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(inventory, stack))){
				return stack;
			}

			int moved = Math.min(stack.getCount(), stack.getMaxStackSize() - inventory.getCount());

			if(!simulate && moved != 0){
				if(inventory.isEmpty()){
					inventory = stack.copy();
					inventory.setCount(moved);
				}else{
					inventory.grow(moved);
				}
				markDirty();
			}

			ItemStack remain = stack.copy();
			remain.shrink(moved);
			return remain;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(slot != 0 || inventory.isEmpty() || amount <= 0){
				return ItemStack.EMPTY;
			}

			ItemStack removed = inventory.copy();
			removed.setCount(Math.min(inventory.getCount(), amount));

			if(!simulate){
				inventory.shrink(removed.getCount());
				markDirty();
			}

			return removed;
		}

		@Override
		public int getSlotLimit(int slot){
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return true;
		}
	}

	@Override
	public int getSizeInventory(){
		return 1;
	}

	@Override
	public boolean isEmpty(){
		return inventory.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index){
		return index == 0 ? inventory : ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int index, int count){
		markDirty();
		return index == 0 ? inventory.split(count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int index){
		if(index == 0){
			ItemStack removed = inventory;
			inventory = ItemStack.EMPTY;
			markDirty();
			return removed;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack){
		if(index == 0){
			inventory = stack;
			markDirty();
		}
	}

	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player){
		return player.getPosition().distanceSq(pos) < 64;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack){
		return index == 0;
	}

	@Override
	public void clear(){
		inventory = ItemStack.EMPTY;
		markDirty();
	}
}
