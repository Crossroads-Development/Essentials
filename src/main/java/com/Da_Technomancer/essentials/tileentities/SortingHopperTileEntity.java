package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.SortingHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SortingHopperTileEntity extends TileEntity implements ITickable, IInventory, IInteractionObject{

	private final ItemStack[] inventory = new ItemStack[5];
	private int transferCooldown = -1;
	private EnumFacing dir = null;

	public SortingHopperTileEntity(){
		super();
		for(int i = 0; i < 5; i++){
			inventory[i] = ItemStack.EMPTY;
		}
	}

	public void resetCache(){
		dir = null;
	}

	private EnumFacing getDir(){
		if(dir == null){
			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() != EssentialsBlocks.sortingHopper){
				return EnumFacing.DOWN;
			}
			dir = state.getValue(SortingHopper.FACING);
		}
		return dir;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void update(){
		if(!world.isRemote && --transferCooldown <= 0){
			transferCooldown = 0;
			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() == EssentialsBlocks.sortingHopper && state.getValue(SortingHopper.ENABLED)){
				boolean flag = false;

				if(!isFull()){
					flag = transferItemsIn();
				}

				if(!isEmpty()){
					flag = transferItemsOut() || flag;
				}

				if(flag){
					transferCooldown = 8;
					markDirty();
				}
			}
		}
	}

	@Override
	public String getName(){
		return "container.sorting_hopper";
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TextComponentTranslation(getName());
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		transferCooldown = nbt.getInteger("trans_cooldown");

		for(int i = 0; i < 5; i++){
			NBTTagCompound stackNBT = nbt.getCompoundTag("inv_" + i);
			inventory[i] = new ItemStack(stackNBT);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);

		for(int i = 0; i < 5; i++){
			if(!inventory[i].isEmpty()){
				NBTTagCompound stackNBT = new NBTTagCompound();
				inventory[i].writeToNBT(stackNBT);
				nbt.setTag("inv_" + i, stackNBT);
			}
		}

		nbt.setInteger("trans_cooldown", transferCooldown);

		return nbt;
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory(){
		return 5;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	public ItemStack getStackInSlot(int index){
		return index > 4 ? ItemStack.EMPTY : inventory[index];
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and
	 * returns them in a new stack.
	 */
	@Override
	public ItemStack decrStackSize(int index, int count){
		if(index > 4 || inventory[index].isEmpty()){
			return ItemStack.EMPTY;
		}
		markDirty();
		return inventory[index].splitStack(count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	public ItemStack removeStackFromSlot(int index){
		if(index > 4){
			return ItemStack.EMPTY;
		}
		ItemStack copy = inventory[index];
		inventory[index] = ItemStack.EMPTY;
		markDirty();
		return copy;
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, ItemStack stack){
		if(index > 4){
			return;
		}
		inventory[index] = stack;
		markDirty();

		if(!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()){
			stack.setCount(stack.getMaxStackSize());
		}
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be
	 * 64, possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes
	 * with Container
	 */
	@Override
	public boolean isUsableByPlayer(EntityPlayer player){
		return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
	}

	@Override
	public void openInventory(EntityPlayer player){

	}

	@Override
	public void closeInventory(EntityPlayer player){

	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack){
		return index < 5;
	}

	@Override
	public boolean isEmpty(){
		for(ItemStack itemstack : inventory){
			if(!itemstack.isEmpty()){
				return false;
			}
		}

		return true;
	}

	private boolean isFull(){
		for(ItemStack itemstack : inventory){
			if(itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()){
				return false;
			}
		}

		return true;
	}

	private boolean transferItemsOut(){
		EnumFacing facing = getDir();
		TileEntity te = world.getTileEntity(pos.offset(facing));

		//Insertion via IItemHandler
		if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())){
			IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
			for(int i = 0; i < getSizeInventory(); i++){
				ItemStack stackInSlot = getStackInSlot(i);
				if(!stackInSlot.isEmpty()){
					ItemStack insert = stackInSlot.copy();
					insert.setCount(1);
					ItemStack newStack = ItemHandlerHelper.insertItem(handler, insert, true);
					if(newStack.isEmpty()){
						ItemHandlerHelper.insertItem(handler, decrStackSize(i, 1), false);
						markDirty();
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean transferItemsIn(){
		TileEntity fromTE = world.getTileEntity(pos.offset(EnumFacing.UP));

		//Transfer from IItemHandler
		if(fromTE != null && fromTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)){
			IItemHandler otherHandler = fromTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);

			for (int i = 0; i < otherHandler.getSlots(); i++){
				ItemStack extractItem = otherHandler.extractItem(i, 1, true);
				if (!extractItem.isEmpty()){

					for (int j = 0; j < getSizeInventory(); j++){
						if(handler.insertItem(j, extractItem, false).isEmpty()){
							otherHandler.extractItem(i, 1, false);
							return true;
						}
					}
				}
			}

			return false;
		}else{
			boolean changed = false;

			//Suck up dropped items
			for(EntityItem entityitem : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.getX(), pos.getY() + 0.5D, pos.getZ(), pos.getX() + 1, pos.getY() + 2D, pos.getZ() + 1), EntitySelectors.IS_ALIVE)){
				if(entityitem == null){
					continue;
				}

				ItemStack stack = entityitem.getItem().copy();
				ItemStack remain = stack;

				for(int i = 0; i < 5; i++){
					remain = handler.insertItem(i, remain, false);
					if(remain.isEmpty()){
						break;
					}
				}

				if(remain.isEmpty()){
					entityitem.setDead();
					changed = true;
				}else if(remain.getCount() != stack.getCount()){
					entityitem.setItem(remain);
					changed = true;
				}
			}
			return changed;
		}
	}

	private static boolean canCombine(ItemStack stack1, ItemStack stack2){
		return stack1.getItem() == stack2.getItem() && stack1.getMetadata() == stack2.getMetadata() && stack1.getCount() <= stack1.getMaxStackSize() && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}

	@Override
	public int getField(int id){
		return 0;
	}

	@Override
	public void setField(int id, int value){

	}

	@Override
	public int getFieldCount(){
		return 0;
	}

	@Override
	public void clear(){
		for(int i = 0; i < 5; ++i){
			inventory[i] = ItemStack.EMPTY;
		}
		markDirty();
	}

	private final ItemHandler handler = new ItemHandler();

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T) handler;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn){
		return new ContainerHopper(playerInventory, this, playerIn);
	}

	@Override
	public String getGuiID(){
		return "minecraft:hopper";
	}

	private class ItemHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return getSizeInventory();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			return SortingHopperTileEntity.this.getStackInSlot(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(stack.isEmpty() || slot > 4){
				return ItemStack.EMPTY;
			}

			if(inventory[slot].isEmpty() || canCombine(stack, inventory[slot])){
				int moved = Math.min(stack.getCount(), stack.getMaxStackSize() - inventory[slot].getCount());
				ItemStack remain = stack.copy();
				remain.shrink(moved);

				if(!simulate && moved != 0){
					if(inventory[slot].isEmpty()){
						inventory[slot] = stack.copy();
						inventory[slot].setCount(moved);
					}else{
						inventory[slot].grow(moved);
					}
					if(transferCooldown < 1){
						transferCooldown = 8;
					}
					markDirty();
				}

				return remain;
			}

			return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(amount <= 0 || slot > 4){
				return ItemStack.EMPTY;
			}

			EnumFacing facing = getDir();

			TileEntity te = world.getTileEntity(pos.offset(facing));
			if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())){
				IItemHandler otherHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				int slots = otherHandler.getSlots();
				for(int i = 0; i < slots; i++){
					if(otherHandler.insertItem(i, inventory[slot], true).getCount() < inventory[slot].getCount()){
						return ItemStack.EMPTY;//Tbe special feature of the sorting hopper is that items can't be drawn from it unless the sorting hopper wouldn't be able to export it.
					}
				}
			}

			int removed = Math.min(amount, inventory[slot].getCount());

			if(!simulate){
				markDirty();
				return inventory[slot].splitStack(removed);
			}

			ItemStack out = inventory[slot].copy();
			out.setCount(removed);
			return out;
		}

		@Override
		public int getSlotLimit(int slot){
			return getInventoryStackLimit();
		}
	}
}
