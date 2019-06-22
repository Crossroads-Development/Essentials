package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(Essentials.MODID)
public class BasicItemSplitterTileEntity extends TileEntity implements ITickable{

	@ObjectHolder("basic_item_splitter")
	private static TileEntityType<BasicItemSplitterTileEntity> TYPE = null;

	public static final int[] MODES = {1, 2, 3};
	private int mode = 1;
	private ItemStack[] inventory = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

	private EnumFacing facing = null;

	public BasicItemSplitterTileEntity(TileEntityType<?> type){
		super(type);
	}

	public BasicItemSplitterTileEntity(){
		this(TYPE);
	}

	private EnumFacing getFacing(){
		if(facing == null){
			IBlockState state = world.getBlockState(pos);
			if(!state.has(EssentialsProperties.FACING)){
				return EnumFacing.DOWN;
			}
			facing = state.get(EssentialsProperties.FACING);
		}
		return facing;
	}

	@Override
	public void tick(){
		EnumFacing dir = getFacing();
		for(int i = 0; i < 2; i++){
			EnumFacing side = i == 0 ? dir : dir.getOpposite();
			TileEntity outputTE = world.getTileEntity(pos.offset(side));
			LazyOptional<IItemHandler> outHandlerCon;
			if(outputTE != null && (outHandlerCon = outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())).isPresent()){
				IItemHandler outHandler = outHandlerCon.orElseThrow(NullPointerException::new);
				for(int j = 0; j < outHandler.getSlots(); j++){
					ItemStack outStack = outHandler.insertItem(j, inventory[i], false);
					if(outStack.getCount() != inventory[i].getCount()){
						inventory[i] = outStack;
						markDirty();
						break;
					}
				}
			}
		}
	}

	public int increaseMode(){
		mode++;
		mode %= MODES.length;
		markDirty();
		return mode;
	}

	private final OutItemHandler primaryHandler = new OutItemHandler(1);
	private final OutItemHandler secondaryHandler = new OutItemHandler(0);
	private final InHandler inHandler = new InHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, EnumFacing side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			EnumFacing dir = getFacing();

			return side == dir ? LazyOptional.of(() -> (T) new OutItemHandler(1)) : side == dir.getOpposite() ? LazyOptional.of(() -> (T) new OutItemHandler(0)) : LazyOptional.of(() -> (T) new InHandler());
		}

		return super.getCapability(cap, side);
	}

	@Override
	public NBTTagCompound write(NBTTagCompound nbt){
		super.write(nbt);
		nbt.setInt("mode", mode);
		nbt.setInt("transfered", transfered);
		for(int i = 0; i < 2; i++){
			if(!inventory[i].isEmpty()){
				NBTTagCompound inner = new NBTTagCompound();
				inventory[i].write(inner);
				nbt.setTag("inv_" + i, inner);
			}
		}
		return nbt;
	}

	@Override
	public void read(NBTTagCompound nbt){
		super.read(nbt);
		mode = nbt.getInt("mode");
		transfered = nbt.getInt("transfered");
		for(int i = 0; i < 2; i++){
			inventory[i] = ItemStack.read(nbt.getCompound("inv_" + i));
		}
	}

	protected int getPortion(){
		return MODES[mode];
	}

	protected int getBase(){
		return 4;
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

		public OutItemHandler(int index){
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
