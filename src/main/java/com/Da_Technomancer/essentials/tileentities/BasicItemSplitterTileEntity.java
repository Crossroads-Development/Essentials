package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BlockEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(Essentials.MODID)
public class BasicItemSplitterBlockEntity extends AbstractSplitterTE{

	@ObjectHolder("basic_item_splitter")
	private static BlockEntityType<BasicItemSplitterBlockEntity> TYPE = null;

	private final ItemStack[] inventory = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};
	private int transferred = 0;//Tracks how many items have been transferred in one batch of 12/15

	public BasicItemSplitterBlockEntity(BlockEntityType<? extends AbstractSplitterTE> type){
		super(type);
	}

	public BasicItemSplitterBlockEntity(){
		this(TYPE);
	}

	@Override
	public void clearCache(){
		super.clearCache();
		primaryOpt.invalidate();
		secondaryOpt.invalidate();
		inOpt.invalidate();
		primaryOpt = LazyOptional.of(() -> new OutItemHandler(1));
		secondaryOpt = LazyOptional.of(() -> new OutItemHandler(0));
		inOpt = LazyOptional.of(InHandler::new);
		endPos[0] = endPos[1] = null;
	}

	@Override
	public void tick(){
		if(endPos[0] == null || endPos[1] == null){
			refreshCache();
		}

		Direction dir = getFacing();
		for(int i = 0; i < 2; i++){
			inventory[i] = AbstractShifterBlockEntity.ejectItem(level, endPos[i], i == 0 ? dir : dir.getOpposite(), inventory[i], null);
		}
		setChanged();
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
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
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.putByte("type", (byte) 1);//Version number for the nbt data
		nbt.putInt("mode", mode);
		nbt.putInt("transferred", transferred);
		for(int i = 0; i < 2; i++){
			if(!inventory[i].isEmpty()){
				CompoundNBT inner = new CompoundNBT();
				inventory[i].save(inner);
				nbt.put("inv_" + i, inner);
			}
		}
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);

		//The way this block saves to nbt was changed in 2.2.0, and a "type" of 1 means the encoding is the new version, while 0 mean old version
		if(nbt.getByte("type") == 1){
			mode = nbt.getInt("mode");
		}else{
			mode = 3 + 3 * nbt.getInt("mode");
		}

		transferred = nbt.getInt("transferred");
		for(int i = 0; i < 2; i++){
			inventory[i] = ItemStack.of(nbt.getCompound("inv_" + i));
		}
	}

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

			int numerator = getMode();
			AbstractSplitterTE.SplitDistribution distribution = getDistribution();
			int denominator = distribution.base;

			int accepted;//How many total qty we accepted
			int goDown;//How many of accepted went down vs up
			int spaceDown = stack.getMaxStackSize() - inventory[0].getCount();
			int spaceUp = stack.getMaxStackSize() - inventory[1].getCount();
			if(numerator == 0){
				accepted = Math.min(spaceUp, stack.getCount());
				goDown = 0;
			}else if(numerator == denominator){
				accepted = Math.min(spaceDown, stack.getCount());
				goDown = accepted;
			}else{
				//Calculate the split for the amount divisible by our base first
				int baseQty = stack.getCount() - stack.getCount() % denominator;
				accepted = denominator * spaceDown / numerator;
				accepted = Math.min(accepted, denominator * spaceUp / (denominator - numerator));
				accepted = Math.max(0, Math.min(baseQty, accepted));//Sanity checks/bounding
				if(accepted % denominator != 0){
					//The direct calculation of goDown is only valid for the portion divisible by the base
					accepted -= accepted % denominator;
				}
				goDown = numerator * accepted / denominator;//Basic portion, before the remainder

				//Tracking of remainder, which follows the pattern in the distribution
				spaceDown -= goDown;
				spaceUp -= (accepted - goDown);
				//Done iteratively, as the pattern is unpredictable and the total remainder is necessarily small (< numerator)
				int remainder = stack.getCount() - accepted;
				for(int i = 0; i < remainder; i++){
					boolean shouldGoDown = distribution.shouldDispense(mode, transferred + i);
					if(shouldGoDown){
						if(spaceDown <= 0){
							//Stop
							break;
						}else{
							spaceDown -= 1;
							goDown += 1;
							accepted += 1;
						}
					}else{
						if(spaceUp <= 0){
							//Stop
							break;
						}else{
							spaceUp -= 1;
							accepted += 1;
						}
					}
				}
			}

//			if(transferred < numerator){
//				goDown += Math.min(numerator - transferred + Math.min((remainder + transferred) % denominator, numerator), remainder);
//			}

			int goUp = accepted - goDown;

			//Actually move the items

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
				transferred += accepted;
				transferred %= denominator;
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
			setChanged();
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
