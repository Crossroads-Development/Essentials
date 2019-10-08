package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendNBTToClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
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
public class SlottedChestTileEntity extends TileEntity implements INBTReceiver, INamedContainerProvider{

	@ObjectHolder("slotted_chest")
	private static TileEntityType<SlottedChestTileEntity> TYPE = null;

	public SlottedChestTileEntity(){
		super(TYPE);
		for(int i = 0; i < 54; i++){
			inv[i] = ItemStack.EMPTY;
			lockedInv[i] = ItemStack.EMPTY;
		}
	}

	private ItemStack[] inv = new ItemStack[54];
	public ItemStack[] lockedInv = new ItemStack[54];

	public float calcComparator(){
		float f = 0.0F;

		for(ItemStack itemstack : inv){
			if(!itemstack.isEmpty()){
				f += (float) itemstack.getCount() / (float) Math.min(64, itemstack.getMaxStackSize());
			}
		}

		f = f / (float) inv.length;
		return f;
	}

	private void filterChanged(){
		if(world.isRemote){
			return;
		}
		CompoundNBT slotNBT = new CompoundNBT();
		for(int i = 0; i < 54; ++i){
			if(!lockedInv[i].isEmpty()){
				slotNBT.put("lock" + i, lockedInv[i].write(new CompoundNBT()));
			}
		}
		BlockUtil.sendClientPacketAround(world, pos, new SendNBTToClient(slotNBT, pos));
	}

	@Override
	public void read(CompoundNBT nbt){
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
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);

		for(int i = 0; i < 54; ++i){
			if(!inv[i].isEmpty()){
				nbt.put("slot" + i, inv[i].write(new CompoundNBT()));
			}
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].write(new CompoundNBT()));
			}
		}

		return nbt;
	}

	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt = super.getUpdateTag();
		for(int i = 0; i < 54; ++i){
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].write(new CompoundNBT()));
			}
		}
		return nbt;
	}

	public final Inventory iInv = new Inventory(inv, lockedInv, this);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return LazyOptional.of(() -> (T) new InventoryHandler());
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
		for(int i = 0; i < 54; i++){
			if(nbt.contains("lock" + i)){
				lockedInv[i] = ItemStack.read(nbt.getCompound("lock" + i));
			}else{
				lockedInv[i] = ItemStack.EMPTY;
			}
		}
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.slotted_chest");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player){
		return new SlottedChestContainer(id, playerInventory, iInv, lockedInv);
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

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return slot < 54 && ItemStack.areItemsEqual(stack, lockedInv[slot]) && ItemStack.areItemStackTagsEqual(stack, lockedInv[slot]);
		}
	}

	public static class Inventory implements IInventory{

		private final ItemStack[] inv;
		private final ItemStack[] lockedInv;
		@Nullable
		private final SlottedChestTileEntity te;

		public Inventory(ItemStack[] inv, ItemStack[] filter, @Nullable SlottedChestTileEntity te){
			this.inv = inv;
			lockedInv = filter;
			this.te = te;
			for(int i = 0; i < inv.length; i++){
				if(inv[i] == null){
					inv[i] = ItemStack.EMPTY;
				}
			}
		}

		@Override
		public int getSizeInventory(){
			return inv.length;
		}

		@Override
		public ItemStack getStackInSlot(int index){
			return index >= inv.length ? ItemStack.EMPTY : inv[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count){
			if(index >= inv.length || inv[index].isEmpty()){
				return ItemStack.EMPTY;
			}

			return inv[index].split(count);
		}

		@Override
		public ItemStack removeStackFromSlot(int index){
			if(index >= inv.length){
				return ItemStack.EMPTY;
			}

			ItemStack stack = inv[index].copy();
			inv[index].setCount(0);
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack){
			if(index < inv.length){
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
			if(te != null){
				te.markDirty();
			}
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity playerEntity){
			return true;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack){
			return index < inv.length && (inv[index].isEmpty() ? lockedInv[index].isEmpty() || SlottedChestContainer.doStackContentsMatch(stack, lockedInv[index]) : SlottedChestContainer.doStackContentsMatch(stack, inv[index]));
		}

		@Override
		public void clear(){
			for(int i = 0; i < inv.length; i++){
				inv[i] = ItemStack.EMPTY;
				lockedInv[i] = ItemStack.EMPTY;
			}
			filterChanged();
		}

		@Override
		public boolean isEmpty(){
			for(ItemStack itemStack : inv){
				if(!itemStack.isEmpty()){
					return false;
				}
			}
			return true;
		}

		public void filterChanged(){
			if(te != null){
				te.filterChanged();
			}
		}
	}
}
