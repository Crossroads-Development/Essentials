package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendSlotFilterToClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

public class SlottedChestTileEntity extends TileEntity implements INBTReceiver{

	@ObjectHolder("slotted_chest")
	private static final TileEntityType<SlottedChestTileEntity> TYPE = null;

	public SlottedChestTileEntity(){
		super(TYPE);
		for(int i = 0; i < 54; i++){
			inv[i] = ItemStack.EMPTY;
			lockedInv[i] = ItemStack.EMPTY;
		}
	}

	private ItemStack[] inv = new ItemStack[54];
	public ItemStack[] lockedInv = new ItemStack[54];

	public void filterChanged(){
		if(world.isRemote){
			return;
		}
		NBTTagCompound slotNBT = new NBTTagCompound();
		for(int i = 0; i < 54; ++i){
			if(!lockedInv[i].isEmpty()){
				slotNBT.put("lock" + i, lockedInv[i].write(new NBTTagCompound()));
			}
		}
		EssentialsPackets.channel.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), 512, world.dimension.getType())), new SendSlotFilterToClient(slotNBT, pos));
	}

	@Override
	public void read(NBTTagCompound nbt){
		super.read(nbt);

		for(int i = 0; i < 54; ++i){
			if(nbt.contains("slot" + i)){
				inv[i] = ItemStack.read(nbt.getCompound("slot" + i));
				//Backward compatibility.
				lockedInv[i] = ItemStack.read(nbt.getCompound("slot" + i));
			}
			if(nbt.contains("lockSlot" + i)){
				lockedInv[i] = ItemStack.read(nbt.getCompound("lockSlot" + i));
			}
		}
	}

	@Override
	public NBTTagCompound write(NBTTagCompound nbt){
		super.write(nbt);

		for(int i = 0; i < 54; ++i){
			if(!inv[i].isEmpty()){
				nbt.put("slot" + i, inv[i].write(new NBTTagCompound()));
			}
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].write(new NBTTagCompound()));
			}
		}

		return nbt;
	}

	@Override
	public NBTTagCompound getUpdateTag(){
		NBTTagCompound nbt = super.getUpdateTag();
		for(int i = 0; i < 54; ++i){
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].write(new NBTTagCompound()));
			}
		}
		return nbt;
	}

	public void cleanPreset(int slot){
		if(slot < 54 && inv[slot].isEmpty()){
			lockedInv[slot] = ItemStack.EMPTY;
		}
		filterChanged();
	}

	public final IInventory iInv = new Inventory();
	private final InventoryHandler handler = new InventoryHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, EnumFacing facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T) handler;
		}

		return super.getCapability(cap, facing);
	}

	public boolean isInventoryType(IInventory inv){
		return inv instanceof Inventory;
	}

	@Override
	public void receiveNBT(NBTTagCompound nbt){
		for(int i = 0; i < 54; i++){
			if(nbt.contains("lock" + i)){
				lockedInv[i] = ItemStack.read(nbt.getCompound("lock" + i));
			}else{
				lockedInv[i] = ItemStack.EMPTY;
			}
		}
	}

	private class InventoryHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 54;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			return slot < 54 ? inv[slot] : ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot >= 54 || stack.isEmpty() || !ItemStack.areItemsEqual(stack, lockedInv[slot])){
				return stack;
			}

			int change = Math.min(stack.getMaxStackSize() - inv[slot].getCount(), stack.getCount());

			if(!simulate){
				if(inv[slot].isEmpty()){
					inv[slot] = stack.copy();
				}else{
					inv[slot].grow(change);
				}
			}

			ItemStack out = stack.copy();
			out.shrink(change);
			return stack.getCount() == change ? ItemStack.EMPTY : out;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(slot >= 54 || inv[slot].isEmpty()){
				return ItemStack.EMPTY;
			}

			int change = Math.min(inv[slot].getCount(), amount);
			ItemStack out = inv[slot].copy();
			out.setCount(change);

			if(!simulate){
				inv[slot].shrink(change);
			}

			return change == 0 ? ItemStack.EMPTY : out;
		}

		@Override
		public int getSlotLimit(int slot){
			return slot < 54 ? 64 : 0;
		}
	}

	private class Inventory implements IInventory{

		@Override
		public String getName(){
			return "container.slotted_chest";
		}

		@Override
		public boolean hasCustomName(){
			return false;
		}

		@Override
		public ITextComponent getDisplayName(){
			return new TextComponentTranslation(getName());
		}

		@Override
		public int getSizeInventory(){
			return 54;
		}

		@Override
		public ItemStack getStackInSlot(int index){
			return index >= 54 ? ItemStack.EMPTY : inv[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count){
			if(index >= 54 || inv[index].isEmpty()){
				return ItemStack.EMPTY;
			}

			return inv[index].split(count);
		}

		@Override
		public ItemStack removeStackFromSlot(int index){
			if(index >= 54){
				return ItemStack.EMPTY;
			}

			ItemStack stack = inv[index].copy();
			inv[index].setCount(0);
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack){
			if(index < 54){
				inv[index] = stack;
				if(!stack.isEmpty()){
					lockedInv[index] = stack.copy();
					lockedInv[index].setCount(1);
				}
			}
		}

		@Override
		public int getInventoryStackLimit(){
			return 64;
		}

		@Override
		public void markDirty(){
			SlottedChestTileEntity.this.markDirty();
		}

		@Override
		public boolean isUsableByPlayer(EntityPlayer player){
			return world.getTileEntity(pos) == SlottedChestTileEntity.this && player.getDistanceSq(pos.add(0.5, 0.5, 0.5)) <= 64;
		}

		@Override
		public void openInventory(EntityPlayer player){
			filterChanged();
		}

		@Override
		public void closeInventory(EntityPlayer player){

		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack){
			return index < 54 && (inv[index].isEmpty() ? lockedInv[index].isEmpty() || SlottedChestContainer.doStackContentsMatch(stack, lockedInv[index]) : SlottedChestContainer.doStackContentsMatch(stack, inv[index]));
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
			for(int i = 0; i < 54; i++){
				inv[i] = ItemStack.EMPTY;
				lockedInv[i] = ItemStack.EMPTY;
			}
			filterChanged();
		}

		@Override
		public boolean isEmpty(){
			for(int i = 0; i < 54; i++){
				if(!inv[i].isEmpty()){
					return false;
				}
			}
			return true;
		}
	}
}
