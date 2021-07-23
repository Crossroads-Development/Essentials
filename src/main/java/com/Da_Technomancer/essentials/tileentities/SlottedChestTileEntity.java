package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendNBTToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ObjectHolder(Essentials.MODID)
public class SlottedChestTileEntity extends BlockEntity implements INBTReceiver, MenuProvider{

	@ObjectHolder("slotted_chest")
	public static BlockEntityType<SlottedChestTileEntity> TYPE = null;

	public SlottedChestTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
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
		if(level.isClientSide){
			return;
		}
		CompoundTag slotNBT = new CompoundTag();
		for(int i = 0; i < 54; ++i){
			if(!lockedInv[i].isEmpty()){
				slotNBT.put("lock" + i, lockedInv[i].save(new CompoundTag()));
			}
		}
		BlockUtil.sendClientPacketAround(level, worldPosition, new SendNBTToClient(slotNBT, worldPosition));
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);

		for(int i = 0; i < 54; ++i){
			if(nbt.contains("slot" + i)){
				inv[i] = ItemStack.of(nbt.getCompound("slot" + i));
				//Backward compatibility.
				lockedInv[i] = ItemStack.of(nbt.getCompound("slot" + i));
			}
			if(nbt.contains("lockSlot" + i)){
				lockedInv[i] = ItemStack.of(nbt.getCompound("lockSlot" + i));
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag nbt){
		super.save(nbt);

		for(int i = 0; i < 54; ++i){
			if(!inv[i].isEmpty()){
				nbt.put("slot" + i, inv[i].save(new CompoundTag()));
			}
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].save(new CompoundTag()));
			}
		}

		return nbt;
	}

	@Override
	public CompoundTag getUpdateTag(){
		CompoundTag nbt = super.getUpdateTag();
		for(int i = 0; i < 54; ++i){
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].save(new CompoundTag()));
			}
		}
		return nbt;
	}

	public final SlottedInv iInv = new SlottedInv(inv, lockedInv, this);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return LazyOptional.of(() -> (T) new InventoryHandler());
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		for(int i = 0; i < 54; i++){
			if(nbt.contains("lock" + i)){
				lockedInv[i] = ItemStack.of(nbt.getCompound("lock" + i));
			}else{
				lockedInv[i] = ItemStack.EMPTY;
			}
		}
	}

	@Override
	public Component getDisplayName(){
		return new TranslatableComponent("container.slotted_chest");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player){
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
			if(slot >= 54 || stack.isEmpty() || !ItemStack.isSame(stack, lockedInv[slot])){
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
			return slot < 54 && ItemStack.isSame(stack, lockedInv[slot]) && ItemStack.tagMatches(stack, lockedInv[slot]);
		}
	}

	public static class SlottedInv implements Container{

		private final ItemStack[] inv;
		private final ItemStack[] lockedInv;
		@Nullable
		private final SlottedChestTileEntity te;

		public SlottedInv(ItemStack[] inv, ItemStack[] filter, @Nullable SlottedChestTileEntity te){
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
		public int getContainerSize(){
			return inv.length;
		}

		@Override
		public ItemStack getItem(int index){
			return index >= inv.length ? ItemStack.EMPTY : inv[index];
		}

		@Override
		public ItemStack removeItem(int index, int count){
			if(index >= inv.length || inv[index].isEmpty()){
				return ItemStack.EMPTY;
			}

			return inv[index].split(count);
		}

		@Override
		public ItemStack removeItemNoUpdate(int index){
			if(index >= inv.length){
				return ItemStack.EMPTY;
			}

			ItemStack stack = inv[index].copy();
			inv[index].setCount(0);
			return stack;
		}

		@Override
		public void setItem(int index, ItemStack stack){
			if(index < inv.length){
				inv[index] = stack;
				if(!stack.isEmpty()){
					lockedInv[index] = stack.copy();
					lockedInv[index].setCount(1);
				}
			}
		}

		@Override
		public int getMaxStackSize(){
			return 64;
		}

		@Override
		public void setChanged(){
			if(te != null){
				te.setChanged();
			}
		}

		@Override
		public boolean stillValid(Player playerEntity){
			return true;
		}

		@Override
		public boolean canPlaceItem(int index, ItemStack stack){
			return index < inv.length && (inv[index].isEmpty() ? lockedInv[index].isEmpty() || SlottedChestContainer.doStackContentsMatch(stack, lockedInv[index]) : SlottedChestContainer.doStackContentsMatch(stack, inv[index]));
		}

		@Override
		public void clearContent(){
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
