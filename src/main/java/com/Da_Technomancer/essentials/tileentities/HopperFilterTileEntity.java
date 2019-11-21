package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendNBTToClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ObjectHolder(Essentials.MODID)
public class HopperFilterTileEntity extends TileEntity implements INBTReceiver{

	@ObjectHolder("hopper_filter")
	private static TileEntityType<HopperFilterTileEntity> TYPE = null;

	public HopperFilterTileEntity(){
		super(TYPE);
	}

	private Direction.Axis axisCache = null;
	private ItemStack filter = ItemStack.EMPTY;

	public ItemStack getFilter(){
		return filter;
	}

	public void setFilter(ItemStack filter){
		this.filter = filter;
		BlockUtil.sendClientPacketAround(world, pos, new SendNBTToClient(filter.write(new CompoundNBT()), pos));
		markDirty();
	}

	private Direction.Axis getAxis(){
		if(axisCache == null){
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() == EssentialsBlocks.hopperFilter){
				axisCache = state.get(EssentialsProperties.AXIS);
			}else{
				return Direction.Axis.Y;
			}
		}
		return axisCache;
	}

	public void clearCache(){
		axisCache = null;
		if(passedHandler != null){
			passedHandler.invalidate();
			passedHandler = null;
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.put("filter", filter.write(new CompoundNBT()));
		return nbt;
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);
		filter = ItemStack.read(nbt.getCompound("filter"));
	}

	@Override
	public CompoundNBT getUpdateTag(){
		return write(super.getUpdateTag());
	}

	@Override
	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
		filter = ItemStack.read(nbt);
	}

	private LazyOptional<IItemHandler> passedHandler = null;

	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side.getAxis() == getAxis()){
			if(passedHandler == null){
				TileEntity te = world.getTileEntity(pos.offset(side.getOpposite()));
				LazyOptional<IItemHandler> src;
				if(te != null && (src = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)).isPresent()){
					passedHandler = LazyOptional.of(() -> new ProxyItemHandler(src));
					src.addListener((handler) -> {if(passedHandler != null) passedHandler.invalidate(); passedHandler = null;});
				}else{
					return LazyOptional.empty();
				}
			}

			return (LazyOptional<T>) passedHandler;
		}

		return super.getCapability(cap, side);
	}

	private static boolean matchFilter(ItemStack query, ItemStack filt){
		if(filt.isEmpty()){
			return false;
		}

		CompoundNBT nbt;
		if(filt.getItem() instanceof BlockItem && ((BlockItem) filt.getItem()).getBlock() instanceof ShulkerBoxBlock && (nbt = filt.getTag()) != null){
			NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(nbt.getCompound("BlockEntityTag"), nonnulllist);

			for(ItemStack singleFilt : nonnulllist){
				if(matchFilter(query, singleFilt)){
					return true;
				}
			}
			return false;
		}
		return query.getItem() == filt.getItem();
	}

	private static class BlankHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 0;
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			return ItemStack.EMPTY;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
			return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return 0;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return false;
		}
	}

	private class ProxyItemHandler implements IItemHandler{

		private final LazyOptional<IItemHandler> src;

		private ProxyItemHandler(LazyOptional<IItemHandler> src){
			this.src = src;
		}

		private IItemHandler getHandler(){
			if(src.isPresent()){
				return src.orElseThrow(NullPointerException::new);
			}else{
				if(passedHandler != null){
					passedHandler.invalidate();
				}
				passedHandler = null;
				return new BlankHandler();
			}
		}

		@Override
		public int getSlots(){
			return getHandler().getSlots();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			return getHandler().getStackInSlot(slot);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
			if(matchFilter(stack, filter)){
				return getHandler().insertItem(slot, stack, simulate);
			}
			return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(matchFilter(getStackInSlot(slot), filter)){
				return getHandler().extractItem(slot, amount, simulate);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return getHandler().getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return matchFilter(stack, filter) && getHandler().isItemValid(slot, stack);
		}
	}
}
