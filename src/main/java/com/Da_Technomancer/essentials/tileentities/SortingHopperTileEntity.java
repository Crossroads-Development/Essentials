package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.SortingHopper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
			BlockState state = world.getBlockState(pos);
			if(!(state.getBlock() instanceof SortingHopper)){
				return Direction.DOWN;
			}
			dir = state.get(SortingHopper.FACING);
		}
		return dir;
	}

	@Override
	public void tick(){
		if(!world.isRemote && --transferCooldown <= 0){
			transferCooldown = 0;
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof SortingHopper && state.get(SortingHopper.ENABLED)){
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
	public void read(CompoundNBT nbt){
		super.read(nbt);
		transferCooldown = nbt.getInt("trans_cooldown");

		for(int i = 0; i < 5; i++){
			CompoundNBT stackNBT = nbt.getCompound("inv_" + i);
			inventory[i] = ItemStack.read(stackNBT);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);

		for(int i = 0; i < 5; i++){
			if(!inventory[i].isEmpty()){
				CompoundNBT stackNBT = new CompoundNBT();
				inventory[i].write(stackNBT);
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
		return inventory[index].split(count);
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
	public boolean isUsableByPlayer(PlayerEntity player){
		return world.getTileEntity(pos) == this && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
	}

	@Override
	public void openInventory(PlayerEntity player){

	}

	@Override
	public void closeInventory(PlayerEntity player){

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
		final IItemHandler otherHandler = getHandlerAtPositon(world, pos.offset(facing),  facing.getOpposite());
		
		//Insertion via IItemHandler
		if(otherHandler != null) {
			for(int i = 0; i < getSizeInventory(); i++){
				ItemStack stackInSlot = getStackInSlot(i);
				if(!stackInSlot.isEmpty()){
					ItemStack insert = stackInSlot.copy();
					insert.setCount(Math.min(insert.getCount(), transferQuantity()));
					ItemStack newStack = ItemHandlerHelper.insertItem(otherHandler, insert, true);
					if(newStack.getCount() < insert.getCount()){
						ItemHandlerHelper.insertItem(otherHandler, decrStackSize(i, insert.getCount() - newStack.getCount()), false);
						markDirty();
						return true;
					}
				}
			}
		}

		return false;
	}
	
	protected boolean transferItemsIn(){
		final IItemHandler otherHandler = getHandlerAtPositon(world, pos.offset(Direction.UP), Direction.DOWN);
		
		//Transfer from IItemHandler
		if(otherHandler != null){
			for (int i = 0; i < otherHandler.getSlots(); i++){
				ItemStack extractItem = otherHandler.extractItem(i, transferQuantity(), true);
				if (!extractItem.isEmpty()){
					for (int j = 0; j < getSizeInventory(); j++){
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
			for(ItemEntity entityitem : world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos.getX(), pos.getY() + 0.5D, pos.getZ(), pos.getX() + 1, pos.getY() + 2D, pos.getZ() + 1), EntityPredicates.IS_ALIVE)){
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
	
	public static IItemHandler getHandlerAtPositon(World world, BlockPos otherPos, Direction direction) {
		IItemHandler otherHandler = null;
		final TileEntity tileEntity = world.getTileEntity(otherPos);
		
		if(tileEntity != null) {
			final LazyOptional<IItemHandler> capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
			if(capability.isPresent()) {
				otherHandler = capability.orElseThrow(NullPointerException::new);
			}
		}
		
		if (otherHandler == null) {
			List<Entity> list = world.getEntitiesInAABBexcluding((Entity) null,	new AxisAlignedBB(otherPos), EntityPredicates.HAS_INVENTORY);
			if (!list.isEmpty()) {
				otherHandler = new InvWrapper((IInventory) list.get(world.rand.nextInt(list.size())));
			}
		}
		return otherHandler;
	}
	
	protected static boolean canCombine(ItemStack stack1, ItemStack stack2){
		return stack1.getItem() == stack2.getItem() && stack1.getCount() <= stack1.getMaxStackSize() && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}

	@Override
	public void clear(){
		for(int i = 0; i < 5; ++i){
			inventory[i] = ItemStack.EMPTY;
		}
		markDirty();
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

			Direction facing = getDir();

			TileEntity te = world.getTileEntity(pos.offset(facing));
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
				markDirty();
				return inventory[slot].split(removed);
			}

			ItemStack out = inventory[slot].copy();
			out.setCount(removed);
			return out;
		}

		@Override
		public int getSlotLimit(int slot){
			return getInventoryStackLimit();
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return true;
		}
	}
}
