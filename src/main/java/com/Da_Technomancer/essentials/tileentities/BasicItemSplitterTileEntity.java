package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(Essentials.MODID)
public class BasicItemSplitterTileEntity extends TileEntity implements ITickableTileEntity{

	@ObjectHolder("basic_item_splitter")
	private static TileEntityType<BasicItemSplitterTileEntity> TYPE = null;

	public static final int[] MODES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	private int mode = 6;
	private ItemStack[] inventory = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

	private Direction facing = null;
	private BlockPos[] endPos = new BlockPos[2];

	public BasicItemSplitterTileEntity(TileEntityType<?> type){
		super(type);
	}

	public BasicItemSplitterTileEntity(){
		this(TYPE);
	}

	private Direction getFacing(){
		if(facing == null){
			BlockState state = world.getBlockState(pos);
			if(!state.has(EssentialsProperties.FACING)){
				return Direction.DOWN;
			}
			facing = state.get(EssentialsProperties.FACING);
		}
		return facing;
	}

	public void refreshCache(){
		facing = null;
		Direction dir = getFacing();
		int maxChutes = EssentialsConfig.itemChuteRange.get();

		for(int i = 0; i < 2; i++){
			int extension;

			for(extension = 1; extension <= maxChutes; extension++){
				BlockState target = world.getBlockState(pos.offset(dir, extension));
				if(target.getBlock() != EssentialsBlocks.itemChute || target.get(EssentialsProperties.AXIS) != dir.getAxis()){
					break;
				}
			}

			endPos[i] = pos.offset(dir, extension);
			dir = dir.getOpposite();
		}
	}

	public void rotate(){
		facing = null;
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

	public int increaseMode(){
		mode++;
		mode %= MODES.length;
		markDirty();
		return mode;
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

	protected int getPortion(){
		return MODES[mode];
	}

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

			int portion = getPortion();
			int base = getBase();

			int accepted = Math.max(0, Math.min(stack.getCount(), portion == 0 ? !inventory[1].isEmpty() && (!ItemStack.areItemsEqual(stack, inventory[1]) || !ItemStack.areItemStackTagsEqual(stack, inventory[1])) ? 0 : stack.getMaxStackSize() - inventory[1].getCount() : portion == base ? !inventory[0].isEmpty() && (!ItemStack.areItemsEqual(stack, inventory[0]) || !ItemStack.areItemStackTagsEqual(stack, inventory[0])) ? 0 : stack.getMaxStackSize() - inventory[0].getCount() : Math.min(!inventory[0].isEmpty() && (!ItemStack.areItemsEqual(stack, inventory[0]) || !ItemStack.areItemStackTagsEqual(stack, inventory[0])) ? 0 : ((base * (stack.getMaxStackSize() - inventory[0].getCount())) / portion), !inventory[1].isEmpty() && (!ItemStack.areItemsEqual(stack, inventory[1]) || !ItemStack.areItemStackTagsEqual(stack, inventory[1])) ? 0 : ((base * (stack.getMaxStackSize() - inventory[1].getCount())) / (base - portion)))));
			int goDown = (portion * (accepted / base)) + (transfered >= portion ? 0 : Math.min(portion - transfered, accepted % base)) + Math.max(0, Math.min(portion, (accepted % base) + transfered - base));
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
				transfered += accepted % base;
				transfered %= base;
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
