package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(Essentials.MODID)
public class BasicItemSplitterTileEntity extends AbstractSplitterTE{

	@ObjectHolder("basic_item_splitter")
	private static TileEntityType<BasicItemSplitterTileEntity> TYPE = null;

	public static final int[] MODES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	private ItemStack[] inventory = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

	public BasicItemSplitterTileEntity(TileEntityType<? extends AbstractSplitterTE> type){
		super(type);
	}

	public BasicItemSplitterTileEntity(){
		this(TYPE);
	}

	@Override
	protected int[] getModes(){
		return MODES;
	}

	@Override
	public void updateContainingBlockInfo(){
		super.updateContainingBlockInfo();
		primaryOpt.invalidate();
		secondaryOpt.invalidate();
		inOpt.invalidate();
		primaryOpt = LazyOptional.of(() -> new OutItemHandler(1));
		secondaryOpt = LazyOptional.of(() -> new OutItemHandler(0));
		inOpt = LazyOptional.of(InHandler::new);
	}

	@Override
	public void tick(){
		if(endPos[0] == null || endPos[1] == null){
			refreshCache();
		}

		Direction dir = getFacing();
		for(int i = 0; i < 2; i++){
			inventory[i] = AbstractShifterTileEntity.ejectItem(world, endPos[i], i == 0 ? dir : dir.getOpposite(), inventory[i]);
		}
		markDirty();
	}

	@Override
	public void remove(){
		super.remove();
		primaryOpt.invalidate();
		secondaryOpt.invalidate();
		inOpt.invalidate();
	}

	private LazyOptional<IItemHandler> primaryOpt = LazyOptional.of(() -> new OutItemHandler(1));
	private LazyOptional<IItemHandler> secondaryOpt = LazyOptional.of(() -> new OutItemHandler(0));
	private LazyOptional<IItemHandler> inOpt = LazyOptional.of(InHandler::new);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			Direction dir = getFacing();

			return (LazyOptional<T>) (side == dir ? primaryOpt : side == dir.getOpposite() ? secondaryOpt : inOpt);
		}

		return super.getCapability(cap, side);
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putByte("type", (byte) 1);//Version number for the nbt data
		nbt.putInt("mode", mode);
		nbt.putInt("transfered", transfered);
		for(int i = 0; i < 2; i++){
			if(!inventory[i].isEmpty()){
				CompoundNBT inner = new CompoundNBT();
				inventory[i].write(inner);
				nbt.put("inv_" + i, inner);
			}
		}
		return nbt;
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);

		//The way this block saves to nbt was changed in 2.2.0, and a "type" of 1 means the encoding is the new version, while 0 mean old version
		if(nbt.getByte("type") == 1){
			mode = nbt.getInt("mode");
		}else{
			mode = 3 + 3 * nbt.getInt("mode");
		}

		transfered = nbt.getInt("transfered");
		for(int i = 0; i < 2; i++){
			inventory[i] = ItemStack.read(nbt.getCompound("inv_" + i));
		}
	}

	@Override
	public int getBase(){
		return 12;
	}

	private int transfered = 0;

	private class InHandler implements IItemHandler{

		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
			if(stack.isEmpty() || slot != 0){
				return stack;
			}

			//Ensure we are allowed to accept
			if(!inventory[0].isEmpty() && !BlockUtil.sameItem(stack, inventory[0]) || !inventory[1].isEmpty() && !BlockUtil.sameItem(stack, inventory[1])){
				return stack;
			}

			int numerator = getActualMode();
			int denominator = getBase();

			int accepted;//How many total qty we can accept
			if(numerator == 0){
				accepted = stack.getMaxStackSize() - inventory[1].getCount();
			}else if(numerator == denominator){
				accepted = stack.getMaxStackSize() - inventory[0].getCount();
			}else{
				accepted = denominator * (stack.getMaxStackSize() - inventory[0].getCount()) / numerator;
				accepted = Math.min(accepted, denominator * (stack.getMaxStackSize() - inventory[1].getCount()) / (denominator - numerator));
			}
			accepted = Math.max(0, Math.min(stack.getCount(), accepted));//Sanity checks/bounding

			int goDown = numerator * (accepted / denominator);//Basic portion, with no need to interact with transferred

			//Tracking of individual remainder with regards to history
			int remainder = accepted % denominator;
			if(transfered < numerator){
				goDown += Math.min(numerator - transfered + Math.min((remainder + transfered) % denominator, numerator), remainder);
			}

			int goUp = accepted - goDown;

			if(!simulate && accepted != 0){
				if(inventory[0].isEmpty()){
					inventory[0] = stack.copy();
					inventory[0].setCount(goDown);
				}else{
					inventory[0].grow(goDown);
				}

				if(inventory[1].isEmpty()){
					inventory[1] = stack.copy();
					inventory[1].setCount(goUp);
				}else{
					inventory[1].grow(goUp);
				}
				transfered += accepted;
				transfered %= denominator;
			}

			if(accepted > 0){
				ItemStack out = stack.copy();
				out.shrink(accepted);
				return out;
			}

			return stack;
		}

		@Override
		public int getSlots(){
			return 1;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			return ItemStack.EMPTY;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return slot == 0;
		}
	}

	protected class OutItemHandler implements IItemHandler{

		private final int index;

		private OutItemHandler(int index){
			this.index = index;
		}

		@Override
		public int getSlots(){
			return 1;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			return slot != 0 ? ItemStack.EMPTY : inventory[index];
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
			return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(slot != 0){
				return ItemStack.EMPTY;
			}

			int moved = Math.min(amount, inventory[index].getCount());
			if(simulate){
				return new ItemStack(inventory[index].getItem(), moved, inventory[index].getTag());
			}
			markDirty();
			return inventory[index].split(moved);
		}

		@Override
		public int getSlotLimit(int slot){
			return 64;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return false;
		}
	}
} 
