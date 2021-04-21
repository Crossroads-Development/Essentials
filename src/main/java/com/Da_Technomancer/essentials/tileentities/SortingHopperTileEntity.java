package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.SortingHopper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@ObjectHolder(Essentials.MODID)
public class SortingHopperTileEntity extends TileEntity implements ITickableTileEntity, IInventory, INamedContainerProvider{

	@ObjectHolder("sorting_hopper")
	private static TileEntityType<SortingHopperTileEntity> TYPE = null;

	protected final ItemStack[] inventory = new ItemStack[5];
	private int transferCooldown = -1;
	private Direction dir = null;

	protected SortingHopperTileEntity(TileEntityType<?> type){
		super(type);
		for(int i = 0; i < 5; i++){
			inventory[i] = ItemStack.EMPTY;
		}
	}

	public SortingHopperTileEntity(){
		this(TYPE);
	}

	public void resetCache(){
		dir = null;
	}

	protected Direction getDir(){
		if(dir == null){
			BlockState state = getBlockState();
			if(!(state.getBlock() instanceof SortingHopper)){
				return Direction.DOWN;
			}
			dir = state.getValue(SortingHopper.FACING);
		}
		return dir;
	}

	@Override
	public void tick(){
		if(!level.isClientSide && --transferCooldown <= 0){
			transferCooldown = 0;
			BlockState state = level.getBlockState(worldPosition);
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
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);
		transferCooldown = nbt.getInt("trans_cooldown");

		for(int i = 0; i < 5; i++){
			CompoundNBT stackNBT = nbt.getCompound("inv_" + i);
			inventory[i] = ItemStack.of(stackNBT);
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);

		for(int i = 0; i < 5; i++){
			if(!inventory[i].isEmpty()){
				CompoundNBT stackNBT = new CompoundNBT();
				inventory[i].save(stackNBT);
				nbt.put("inv_" + i, stackNBT);
			}
		}

		nbt.putInt("trans_cooldown", transferCooldown);

		return nbt;
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
	public boolean stillValid(PlayerEntity player){
		return level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64D;
	}

	@Override
	public void startOpen(PlayerEntity player){

	}

	@Override
	public void stopOpen(PlayerEntity player){

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
		Direction facing = getDir();
		final IItemHandler otherHandler = getHandlerAtPosition(level, worldPosition.relative(facing), facing.getOpposite(), null);

		//Insertion via IItemHandler
		if(otherHandler != null){
			for(int i = 0; i < getContainerSize(); i++){
				ItemStack stackInSlot = getItem(i);
				if(!stackInSlot.isEmpty()){
					ItemStack insert = stackInSlot.copy();
					insert.setCount(Math.min(insert.getCount(), transferQuantity()));
					ItemStack newStack = ItemHandlerHelper.insertItem(otherHandler, insert, true);
					if(newStack.getCount() < insert.getCount()){
						ItemHandlerHelper.insertItem(otherHandler, removeItem(i, insert.getCount() - newStack.getCount()), false);
						setChanged();
						return true;
					}
				}
			}
		}

		return false;
	}

	protected boolean transferItemsIn(){
		BlockPos upPos = worldPosition.above();
		TileEntity aboveTE = level.getBlockEntity(upPos);
		final IItemHandler otherHandler = getHandlerAtPosition(level, upPos, Direction.DOWN, aboveTE);

		//Transfer from IItemHandler
		if(otherHandler != null){
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
			boolean changed = false;

			//Suck up dropped items
			List<ItemEntity> itemEntities;

			//If the block above is a Hopper Filter, we can pick up items through the filter, but only if they match the filter
			if(aboveTE instanceof HopperFilterTileEntity){
				ItemStack filter = ((HopperFilterTileEntity) aboveTE).getFilter();
				itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(worldPosition.getX(), worldPosition.getY() + 0.5D, worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3D, worldPosition.getZ() + 1), entity -> entity.isAlive() && HopperFilterTileEntity.matchFilter(entity.getItem(), filter));
			}else{
				itemEntities = level.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(worldPosition.getX(), worldPosition.getY() + 0.5D, worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 2D, worldPosition.getZ() + 1), EntityPredicates.ENTITY_STILL_ALIVE);
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
					entityitem.remove();
					changed = true;
				}else if(remain.getCount() != stack.getCount()){
					entityitem.setItem(remain);
					changed = true;
				}
			}
			return changed;
		}
	}

	protected static IItemHandler getHandlerAtPosition(World world, BlockPos otherPos, Direction direction, @Nullable TileEntity aboveTE){
		final TileEntity te = aboveTE == null ? world.getBlockEntity(otherPos) : aboveTE;

		if(te != null){
			final LazyOptional<IItemHandler> capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
			if(capability.isPresent()){
				IItemHandler handler = capability.orElseThrow(NullPointerException::new);
				//This slot count check enables sorting hoppers to pull items from the world through a hopper filter when there is no inventory on the other side
				if(handler.getSlots() > 0){
					return handler;
				}
			}
		}

		//In vanilla, this is literally just composters
		BlockState state = world.getBlockState(otherPos);
		if(state.getBlock() instanceof ISidedInventoryProvider){
			ISidedInventory inv = ((ISidedInventoryProvider) state.getBlock()).getContainer(state, world, otherPos);
			return new InvWrapper(inv);
		}

		List<Entity> list = world.getEntities((Entity) null, new AxisAlignedBB(otherPos), EntityPredicates.CONTAINER_ENTITY_SELECTOR);
		if(!list.isEmpty()){
			return new InvWrapper((IInventory) list.get(world.random.nextInt(list.size())));
		}

		return null;
	}

	protected static boolean canCombine(ItemStack stack1, ItemStack stack2){
		return stack1.getItem() == stack2.getItem() && stack1.getCount() <= stack1.getMaxStackSize() && ItemStack.tagMatches(stack1, stack2);
	}

	@Override
	public void clearContent(){
		for(int i = 0; i < 5; ++i){
			inventory[i] = ItemStack.EMPTY;
		}
		setChanged();
	}

	protected ItemHandler handler = new ItemHandler();

	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing){
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return LazyOptional.of(() -> (T) handler);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.sorting_hopper");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player){
		return new HopperContainer(id, playerInventory, this);
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

			Direction facing = getDir();

			TileEntity te = level.getBlockEntity(worldPosition.relative(facing));
			LazyOptional<IItemHandler> otherCap;
			if(te != null && (otherCap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())).isPresent()){
				IItemHandler otherHandler = otherCap.orElseThrow(NullPointerException::new);
				int slots = otherHandler.getSlots();
				for(int i = 0; i < slots; i++){
					if(otherHandler.insertItem(i, inventory[slot], true).getCount() < inventory[slot].getCount()){
						return ItemStack.EMPTY;//Tbe special feature of the sorting hopper is that items can't be drawn from it unless the sorting hopper wouldn't be able to export it.
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
