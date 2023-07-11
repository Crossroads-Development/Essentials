package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.IItemContainer;
import com.Da_Technomancer.essentials.api.packets.INBTReceiver;
import com.Da_Technomancer.essentials.api.packets.SendNBTToClient;
import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.slottedChest;

public class SlottedChestTileEntity extends BlockEntity implements INBTReceiver, MenuProvider, IItemContainer{

	public static final BlockEntityType<SlottedChestTileEntity> TYPE = ESTileEntity.createType(SlottedChestTileEntity::new, slottedChest);

	public SlottedChestTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
		Arrays.fill(inv, ItemStack.EMPTY);
		Arrays.fill(lockedInv, ItemStack.EMPTY);
	}

	private final ItemStack[] inv = new ItemStack[54];
	public ItemStack[] lockedInv = new ItemStack[inv.length];

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

	private void updateFilter(){
		boolean syncToClient = !level.isClientSide;
		CompoundTag slotNBT = new CompoundTag();
		for(int i = 0; i < inv.length; ++i){
			if(!inv[i].isEmpty() && !BlockUtil.sameItem(inv[i], lockedInv[i])){
				lockedInv[i] = inv[i].copy();
				lockedInv[i].setCount(1);
			}
			if(syncToClient && !lockedInv[i].isEmpty()){
				slotNBT.put("lock" + i, lockedInv[i].save(new CompoundTag()));
			}
		}
		if(syncToClient){
			BlockUtil.sendClientPacketAround(level, worldPosition, new SendNBTToClient(slotNBT, worldPosition));
		}
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);

		for(int i = 0; i < inv.length; ++i){
			if(nbt.contains("slot" + i)){
				inv[i] = ItemStack.of(nbt.getCompound("slot" + i));
			}else{
				inv[i] = ItemStack.EMPTY;
			}
			if(nbt.contains("lockSlot" + i)){
				lockedInv[i] = ItemStack.of(nbt.getCompound("lockSlot" + i));
			}else{
				inv[i] = ItemStack.EMPTY;
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);

		for(int i = 0; i < inv.length; ++i){
			if(!inv[i].isEmpty()){
				nbt.put("slot" + i, inv[i].save(new CompoundTag()));
			}
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].save(new CompoundTag()));
			}
		}
	}

	@Override
	public CompoundTag getUpdateTag(){
		CompoundTag nbt = super.getUpdateTag();
		for(int i = 0; i < inv.length; ++i){
			if(!lockedInv[i].isEmpty()){
				nbt.put("lockSlot" + i, lockedInv[i].save(new CompoundTag()));
			}
		}
		return nbt;
	}

	private final LazyOptional<IItemHandler> invOptional = LazyOptional.of(InventoryHandler::new);

	@Override
	public void setRemoved(){
		super.setRemoved();
		invOptional.invalidate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing){
		if(cap == ForgeCapabilities.ITEM_HANDLER){
			return (LazyOptional<T>) invOptional;
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		for(int i = 0; i < inv.length; i++){
			if(nbt.contains("lock" + i)){
				lockedInv[i] = ItemStack.of(nbt.getCompound("lock" + i));
			}else{
				lockedInv[i] = ItemStack.EMPTY;
			}
		}
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.slotted_chest");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player){
		FriendlyByteBuf buf = createContainerBuf();
		for(ItemStack lock : lockedInv){
			buf.writeItem(lock);
		}
		return new SlottedChestContainer(id, playerInventory, buf);
	}

	@Override
	public int[] getSlotsForFace(Direction dir){
		int[] out = new int[inv.length];
		for(int i = 0; i < out.length; i++){
			out[i] = i;
		}
		return out;
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction dir){
		return index >= 0 && index < inv.length;
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
		setChanged();
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
			setChanged();
			inv[index] = stack;
			if(!stack.isEmpty()){
				lockedInv[index] = stack.copy();
				lockedInv[index].setCount(1);
			}
		}
	}

	@Override
	public void setChanged(){
		SlottedChestTileEntity.this.updateFilter();
		super.setChanged();
	}

	@Override
	public boolean stillValid(Player playerEntity){
		return true;
	}

	/**
	 * Used for machine item transfer, shift-clicking in the UI
	 */
	@Override
	public boolean canPlaceItem(int index, ItemStack stack){
		//Refuses if slot is unlocked
		return index < inv.length && BlockUtil.sameItem(stack, lockedInv[index]);
	}

	/**
	 * Used for most actions in the UI (other than shift-clicking)
	 */
	public boolean canPlaceItemUI(int index, ItemStack stack){
		//Allows if slot is unlocked
		return index < inv.length && (BlockUtil.sameItem(stack, lockedInv[index]) || lockedInv[index].isEmpty());
	}

	@Override
	public void clearContent(){
		Arrays.fill(inv, ItemStack.EMPTY);
		Arrays.fill(lockedInv, ItemStack.EMPTY);
		setChanged();
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

	private class InventoryHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return inv.length;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			return getItem(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(!isItemValid(slot, stack) || stack.isEmpty() || !inv[slot].isEmpty() && !BlockUtil.sameItem(stack, inv[slot])){
				return stack;
			}

			int change = Math.min(stack.getMaxStackSize() - inv[slot].getCount(), stack.getCount());

			if(!simulate){
				if(inv[slot].isEmpty()){
					inv[slot] = stack.copy();
				}else{
					inv[slot].grow(change);
				}
				setChanged();
			}

			ItemStack out = stack.copy();
			out.shrink(change);
			return stack.getCount() == change ? ItemStack.EMPTY : out;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(slot >= inv.length || inv[slot].isEmpty()){
				return ItemStack.EMPTY;
			}

			int change = Math.min(inv[slot].getCount(), amount);
			ItemStack out = inv[slot].copy();
			out.setCount(change);

			if(!simulate){
				inv[slot].shrink(change);
				setChanged();
			}

			return change == 0 ? ItemStack.EMPTY : out;
		}

		@Override
		public int getSlotLimit(int slot){
			return getMaxStackSize(slot);
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return canPlaceItem(slot, stack);
		}
	}
}
