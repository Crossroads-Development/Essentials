package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemShifterTileEntity extends TileEntity implements ITickable, IInventory{

	private ItemStack inventory = ItemStack.EMPTY;
	private BlockPos endPos = null;

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void update(){
		if(world.isRemote){
			return;
		}

		if(endPos == null){
			refreshCache();
		}

		IBlockState state = world.getBlockState(pos);
		if(inventory.isEmpty() || state.getBlock() != EssentialsBlocks.itemShifter){
			return;
		}

		TileEntity outputTE = world.getTileEntity(endPos);
		if(outputTE != null && outputTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, state.getValue(EssentialsProperties.FACING).getOpposite())){
			IItemHandler outHandler = outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, state.getValue(EssentialsProperties.FACING).getOpposite());
			for(int i = 0; i < outHandler.getSlots(); i++){
				ItemStack outStack = outHandler.insertItem(i, inventory, false);
				if(outStack.getCount() != inventory.getCount()){
					inventory = outStack;
					markDirty();
					return;
				}
			}
			return;
		}

		EntityItem ent = new EntityItem(world, endPos.getX() + 0.5D, endPos.getY() + 0.5D, endPos.getZ() + 0.5D, inventory);
		ent.motionX = 0;
		ent.motionZ = 0;
		world.spawnEntity(ent);
		inventory = ItemStack.EMPTY;
		markDirty();
	}

	public void refreshCache(){
		EnumFacing dir = world.getBlockState(pos).getValue(EssentialsProperties.FACING);
		int extension = 1;
		int maxChutes = EssentialsConfig.getConfigInt(EssentialsConfig.itemChuteRange, false);

		for(; extension <= maxChutes; extension++){
			IBlockState target = world.getBlockState(pos.offset(dir, extension));
			if(target.getBlock() != EssentialsBlocks.itemChute || target.getValue(EssentialsProperties.AXIS) != dir.getAxis()){
				break;
			}
		}

		endPos = pos.offset(dir, extension);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);

		if(!inventory.isEmpty()){
			nbt.setTag("inv", inventory.writeToNBT(new NBTTagCompound()));
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);

		if(nbt.hasKey("inv")){
			inventory = new ItemStack(nbt.getCompoundTag("inv"));
		}
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return true;
		}

		return super.hasCapability(cap, facing);
	}

	private final IItemHandler handler = new InventoryHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T) handler;
		}

		return super.getCapability(cap, facing);
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

			if(!simulate){
				if(inventory.isEmpty()){
					inventory = stack.copy();
					inventory.setCount(moved);
				}else{
					inventory.grow(1);
				}
				markDirty();
			}

			ItemStack remain = stack.copy();
			remain.shrink(moved);
			return remain;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(slot != 0 || inventory.isEmpty()){
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
			return slot == 0 ? 64 : 0;
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
		return index == 0 ? inventory.splitStack(count) : ItemStack.EMPTY;
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
	public boolean isUsableByPlayer(EntityPlayer player){
		return player.getPosition().distanceSq(pos) < 64;
	}

	@Override
	public void openInventory(EntityPlayer player){

	}

	@Override
	public void closeInventory(EntityPlayer player){

	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack){
		return index == 0;
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
		inventory = ItemStack.EMPTY;
		markDirty();
	}

	@Override
	public String getName(){
		return "Item Shifter";
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TextComponentString(getName());
	}
}
