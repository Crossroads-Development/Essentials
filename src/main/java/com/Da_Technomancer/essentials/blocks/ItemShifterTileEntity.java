package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.gui.container.ItemShifterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
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

import static com.Da_Technomancer.essentials.blocks.ESBlocks.itemShifter;

public class ItemShifterTileEntity extends AbstractShifterTileEntity implements Container{

	public static final BlockEntityType<ItemShifterTileEntity> TYPE = ESTileEntity.createType(ItemShifterTileEntity::new, itemShifter);

	private ItemStack inventory = ItemStack.EMPTY;
	private LazyOptional<IItemHandler> outputOptionalCache = LazyOptional.empty();

	public ItemShifterTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	public void serverTick(){
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
				outputOptionalCache = endTE.getCapability(ForgeCapabilities.ITEM_HANDLER, getFacing().getOpposite());
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
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);

		if(!inventory.isEmpty()){
			nbt.put("inv", inventory.save(new CompoundTag()));
		}
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);

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
		if(cap == ForgeCapabilities.ITEM_HANDLER){
			return (LazyOptional<T>) invOptional;
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.item_shifter");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player){
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
			if(slot != 0 || stack.isEmpty() || !inventory.isEmpty() && !BlockUtil.sameItem(inventory, stack)){
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
		return worldPosition.distToCenterSqr(player.position()) < 64;
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
