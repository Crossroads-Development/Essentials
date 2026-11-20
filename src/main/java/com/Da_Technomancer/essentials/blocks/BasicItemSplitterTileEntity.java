package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.IItemCapable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.basicItemSplitter;

public class BasicItemSplitterTileEntity extends AbstractSplitterTE<IItemHandler> implements IItemCapable{

	public static final BlockEntityType<BasicItemSplitterTileEntity> TYPE = ESTileEntity.createType(BasicItemSplitterTileEntity::new, basicItemSplitter);

	private final ItemStack[] inventory = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};
	private int transferred = 0;//Tracks how many items have been transferred in one batch of 12/15

	public BasicItemSplitterTileEntity(BlockEntityType<? extends AbstractSplitterTE> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}

	public BasicItemSplitterTileEntity(BlockPos pos, BlockState state){
		this(TYPE, pos, state);
	}

	@Override
	protected BlockCapability<IItemHandler, Direction> getCapability(){
		return Capabilities.ItemHandler.BLOCK;
	}

	@Override
	public void serverTick(){
		if(endPos[0] == null || endPos[1] == null){
			refreshCache();
		}

		for(int i = 0; i < 2; i++){
			inventory[i] = AbstractShifterTileEntity.ejectItem(level, endPos[i], inventory[i], outputCache[i]);
		}
		setChanged();
	}

	private final IItemHandler primaryHandler = new OutItemHandler(1);
	private final IItemHandler secondaryHandler = new OutItemHandler(0);
	private final IItemHandler inHandler = new InHandler();

	@Nullable
	@Override
	public IItemHandler getItemHandler(Direction side){
		Direction dir = BlockUtil.evaluateProperty(getBlockState(), ESProperties.FACING, Direction.DOWN);
		return side == dir ? primaryHandler : side == dir.getOpposite() ? secondaryHandler : inHandler;
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.saveAdditional(nbt, registries);
//		nbt.putByte("type", (byte) 1);//Version number for the nbt data
		nbt.putInt("mode", mode);
		nbt.putInt("transferred", transferred);
		for(int i = 0; i < 2; i++){
			if(!inventory[i].isEmpty()){
				nbt.put("inv_" + i, BlockUtil.stackToNBT(inventory[i], registries));
			}
		}
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.loadAdditional(nbt, registries);

		//The way this block saves to nbt was changed in 2.2.0, and a "type" of 1 means the encoding is the new version, while 0 mean old version
//		if(nbt.getByte("type") == 1){
		mode = nbt.getInt("mode");
//		}else{
//			mode = 3 + 3 * nbt.getInt("mode");
//		}

		transferred = nbt.getInt("transferred");
		for(int i = 0; i < 2; i++){
			inventory[i] = BlockUtil.nbtToItemStack(nbt.getCompound("inv_" + i), registries);
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
					boolean shouldGoDown = distribution.shouldDispense(getMode(), transferred + i);
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
				return inventory[index].copyWithCount(moved);
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
