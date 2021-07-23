package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.ItemShifterContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.tileentity.BlockEntityType;
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
public class ItemShifterBlockEntity extends AbstractShifterBlockEntity implements IInventory{

	@ObjectHolder("item_shifter")
	private static BlockEntityType<ItemShifterBlockEntity> TYPE = null;

	private ItemStack inventory = ItemStack.EMPTY;
	private LazyOptional<IItemHandler> outputOptionalCache = LazyOptional.empty();

	public ItemShifterBlockEntity(){
		super(TYPE);
	}

	@Override
	public void tick(){
		if(level.isClientSide){
			return;
		}

		if(endPos == null){
			refreshCache();
		}

		if(inventory.isEmpty()){
			return;
		}

		//We use a cache for the output, which the ejectItem method will use instead of checking for the TE independently
		if(!outputOptionalCache.isPresent()){
			BlockEntity endTE = level.getBlockEntity(endPos);
			if(endTE != null){
				outputOptionalCache = endTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing().getOpposite());
			}
		}

		inventory = ejectItem(level, endPos, getFacing(), inventory, outputOptionalCache);
	}

	@Override
	public void refreshCache(){
		super.refreshCache();
		outputOptionalCache = LazyOptional.empty();
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);

		if(!inventory.isEmpty()){
			nbt.put("inv", inventory.save(new CompoundNBT()));
		}
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);

		if(nbt.contains("inv")){
			inventory = ItemStack.of(nbt.getCompound("inv"));
		}
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
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
	public Container createMenu(int id, PlayerInventory playerInventory, Player player){
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
			if(slot != 0 || stack.isEmpty() || !inventory.isEmpty() && (!inventory.sameItem(stack) || !ItemStack.tagMatches(inventory, stack))){
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
				setChanged();
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
				setChanged();
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
	public int getContainerSize(){
		return 1;
	}

	@Override
	public boolean isEmpty(){
		return inventory.isEmpty();
	}

	@Override
	public ItemStack getItem(int index){
		return index == 0 ? inventory : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int index, int count){
		setChanged();
		return index == 0 ? inventory.split(count) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index){
		if(index == 0){
			ItemStack removed = inventory;
			inventory = ItemStack.EMPTY;
			setChanged();
			return removed;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int index, ItemStack stack){
		if(index == 0){
			inventory = stack;
			setChanged();
		}
	}

	@Override
	public int getMaxStackSize(){
		return 64;
	}

	@Override
	public boolean stillValid(Player player){
		return worldPosition.distSqr(player.position(), true) < 64;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack){
		return index == 0;
	}

	@Override
	public void clearContent(){
		inventory = ItemStack.EMPTY;
		setChanged();
	}
}
