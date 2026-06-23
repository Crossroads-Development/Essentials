package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.IItemCapable;
import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.sortingHopper;

public class SortingHopperTileEntity extends BlockEntity implements ITickableTileEntity, Container, MenuProvider, IItemCapable{

	public static final BlockEntityType<SortingHopperTileEntity> TYPE = ESTileEntity.createType(SortingHopperTileEntity::new, sortingHopper);

	protected final ItemStack[] inventory = new ItemStack[5];
	private int transferCooldown = -1;
	private BlockCapabilityCache<IItemHandler, Direction> inputCache;
	private BlockCapabilityCache<IItemHandler, Direction> outputCache;

	protected SortingHopperTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
		super(type, pos, state);
		for(int i = 0; i < 5; i++){
			inventory[i] = ItemStack.EMPTY;
		}
	}

	public SortingHopperTileEntity(BlockPos pos, BlockState state){
		this(TYPE, pos, state);
	}

	public void resetCache(){
		level.invalidateCapabilities(worldPosition);
		outputCache = null;
	}

	private IItemHandler getOutputHandler(){
		if(outputCache == null){
			outputCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) level, worldPosition.relative(getBlockState().getValue(SortingHopper.FACING)), getBlockState().getValue(SortingHopper.FACING).getOpposite());
		}
		IItemHandler result = outputCache.getCapability();
		if(result == null){
			Direction dir = getBlockState().getValue(SortingHopper.FACING);
			result = getEntityItemHandlerAtPosition(level, worldPosition.relative(dir), dir.getOpposite());
		}
		return result;
	}

	private IItemHandler getInputHandler(){
		if(inputCache == null){
			inputCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) level, worldPosition.above(), Direction.DOWN);
		}
		IItemHandler result = inputCache.getCapability();
		if(result == null){
			result = getEntityItemHandlerAtPosition(level, worldPosition.below(), Direction.UP);
		}
		return result;
	}

	private static IItemHandler getEntityItemHandlerAtPosition(Level world, BlockPos otherPos, Direction side){
		List<Entity> list = world.getEntities((Entity) null, new AABB(otherPos), EntitySelector.ENTITY_STILL_ALIVE);
		if(!list.isEmpty()){
			Collections.shuffle(list);
			Iterator<Entity> entityIter = list.iterator();

			while(entityIter.hasNext()){
				Entity entity = entityIter.next();
				IItemHandler entityCap = entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side);
				if(entityCap != null){
					return entityCap;
				}
			}
		}
		return null;
	}

	@Override
	public void serverTick(){
		if(--transferCooldown <= 0){
			transferCooldown = 0;
			BlockState state = getBlockState();
			if(state.getBlock() instanceof SortingHopper && state.getValue(SortingHopper.ENABLED)){
				boolean flag = false;

				if(!isFull()){
					flag = transferItemsIn();
				}

				if(!isEmpty()){
					flag = transferItemsOut() || flag;
				}

				if(flag){
					transferCooldown = 8;
					setChanged();
				}
			}
		}
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.loadAdditional(nbt, registries);
		transferCooldown = nbt.getInt("trans_cooldown");

		for(int i = 0; i < 5; i++){
			CompoundTag stackNBT = nbt.getCompound("inv_" + i);
			inventory[i] = BlockUtil.nbtToItemStack(stackNBT, registries);
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.saveAdditional(nbt, registries);

		for(int i = 0; i < 5; i++){
			if(!inventory[i].isEmpty()){
				nbt.put("inv_" + i, BlockUtil.stackToNBT(inventory[i], registries));
			}
		}

		nbt.putInt("trans_cooldown", transferCooldown);
	}

	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getContainerSize(){
		return 5;
	}

	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	public ItemStack getItem(int index){
		return index > 4 ? ItemStack.EMPTY : inventory[index];
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and
	 * returns them in a new stack.
	 */
	@Override
	public ItemStack removeItem(int index, int count){
		if(index > 4 || inventory[index].isEmpty()){
			return ItemStack.EMPTY;
		}
		setChanged();
		return inventory[index].split(count);
	}

	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	public ItemStack removeItemNoUpdate(int index){
		if(index > 4){
			return ItemStack.EMPTY;
		}
		ItemStack copy = inventory[index];
		inventory[index] = ItemStack.EMPTY;
		setChanged();
		return copy;
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	@Override
	public void setItem(int index, ItemStack stack){
		if(index > 4){
			return;
		}
		inventory[index] = stack;
		setChanged();

		if(!stack.isEmpty() && stack.getCount() > stack.getMaxStackSize()){
			stack.setCount(stack.getMaxStackSize());
		}
	}

	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be
	 * 64, possibly will be extended.
	 */
	@Override
	public int getMaxStackSize(){
		return 64;
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes
	 * with Container
	 */
	@Override
	public boolean stillValid(Player player){
		return BlockUtil.playerInRangeOfUI(player, this);
	}

	@Override
	public void startOpen(Player player){

	}

	@Override
	public void stopOpen(Player player){

	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack){
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

	protected boolean isFull(){
		for(ItemStack itemstack : inventory){
			if(itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()){
				return false;
			}
		}

		return true;
	}

	protected int transferQuantity(){
		return 1;
	}

	protected boolean transferItemsOut(){
		final IItemHandler otherHandler = getOutputHandler();

		//Insertion via IItemHandler
		if(otherHandler != null){
			for(int i = 0; i < getContainerSize(); i++){
				ItemStack stackInSlot = getItem(i);
				if(!stackInSlot.isEmpty()){
					ItemStack insert = stackInSlot.copy();
					insert.setCount(Math.min(insert.getCount(), transferQuantity()));
					//newStack is uninserted remainder
					ItemStack newStack = ItemHandlerHelper.insertItem(otherHandler, insert, true);//Simulate the transfer
					if(newStack.getCount() < insert.getCount()){
						insert.setCount(insert.getCount() - newStack.getCount());//Only attempt to insert the number accepted in the simulated transfer
						newStack = ItemHandlerHelper.insertItem(otherHandler, insert, false);//Actually perform the transfer
						//Typically, newStack is empty, but it is not guaranteed.
						//Remove the items that were actually inserted
						if(!removeItem(i, insert.getCount() - newStack.getCount()).isEmpty()){
							//True for items actually moved
							setChanged();
							return true;
						}
						return false;
					}
				}
			}
		}

		return false;
	}

	protected boolean transferItemsIn(){
		final IItemHandler otherHandler = getInputHandler();

		//Transfer from IItemHandler
		if(otherHandler != null && !(otherHandler instanceof HopperFilterTileEntity.ProxyItemHandler proxyItemHandler && proxyItemHandler.isNakedHandler())){
			for(int i = 0; i < otherHandler.getSlots(); i++){
				ItemStack extractItem = otherHandler.extractItem(i, transferQuantity(), true);
				if(!extractItem.isEmpty()){
					for(int j = 0; j < getContainerSize(); j++){
						ItemStack uninserted = handler.insertItem(j, extractItem, false);
						if(uninserted.getCount() < extractItem.getCount()){
							otherHandler.extractItem(i, extractItem.getCount() - uninserted.getCount(), false);
							return true;
						}
					}
				}
			}

			return false;
		}else{
			BlockEntity aboveTE = level.getBlockEntity(worldPosition.above());
			boolean changed = false;

			//Suck up dropped items
			List<ItemEntity> itemEntities;

			//If the block above is a Hopper Filter, we can pick up items through the filter, but only if they match the filter
			if(aboveTE instanceof HopperFilterTileEntity filterTE){
				itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition.getX(), worldPosition.getY() + 0.5D, worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3D, worldPosition.getZ() + 1), entity -> entity.isAlive() && filterTE.matchFilter(entity.getItem()));
			}else{
				itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition.getX(), worldPosition.getY() + 0.5D, worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 2D, worldPosition.getZ() + 1), EntitySelector.ENTITY_STILL_ALIVE);
			}

			for(ItemEntity entityitem : itemEntities){
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
					entityitem.remove(Entity.RemovalReason.DISCARDED);
					changed = true;
				}else if(remain.getCount() != stack.getCount()){
					entityitem.discard();
					changed = true;
				}
			}
			return changed;
		}
	}

	protected static boolean canCombine(ItemStack stack1, ItemStack stack2){
		return BlockUtil.sameItem(stack1, stack2) && stack1.getCount() <= stack1.getMaxStackSize();
	}

	@Override
	public void clearContent(){
		for(int i = 0; i < 5; ++i){
			inventory[i] = ItemStack.EMPTY;
		}
		setChanged();
	}

	protected ItemHandler handler = new ItemHandler();

	@Nullable
	@Override
	public IItemHandler getItemHandler(Direction dir){
		return handler;
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.sorting_hopper");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player){
		return new HopperMenu(id, playerInventory, this);
	}

	protected class ItemHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return getContainerSize();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			return SortingHopperTileEntity.this.getItem(slot);
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
					setChanged();
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

			IItemHandler otherHandler = getOutputHandler();
			if(otherHandler != null){
				int slots = otherHandler.getSlots();
				for(int i = 0; i < slots; i++){
					if(otherHandler.insertItem(i, inventory[slot], true).getCount() < inventory[slot].getCount()){
						return ItemStack.EMPTY;//The special feature of the sorting hopper is that items can't be drawn from it unless the sorting hopper wouldn't be able to export it.
					}
				}
			}

			int removed = Math.min(amount, inventory[slot].getCount());

			if(!simulate){
				setChanged();
				return inventory[slot].split(removed);
			}

			ItemStack out = inventory[slot].copy();
			out.setCount(removed);
			return out;
		}

		@Override
		public int getSlotLimit(int slot){
			return getMaxStackSize();
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return true;
		}
	}
}
