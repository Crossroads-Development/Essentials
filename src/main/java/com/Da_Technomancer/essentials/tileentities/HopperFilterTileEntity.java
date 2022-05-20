package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendNBTToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

@ObjectHolder(Essentials.MODID)
public class HopperFilterTileEntity extends BlockEntity implements INBTReceiver{

	@ObjectHolder("hopper_filter")
	public static BlockEntityType<HopperFilterTileEntity> TYPE = null;

	public HopperFilterTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	private Direction.Axis axisCache = null;
	private ItemStack filter = ItemStack.EMPTY;
	private Set<Item> filterItemsCache = null;

	public ItemStack getFilter(){
		return filter;
	}

	public void setFilter(ItemStack filter){
		this.filter = filter;
		filterItemsCache = null;
		BlockUtil.sendClientPacketAround(level, worldPosition, new SendNBTToClient(filter.save(new CompoundTag()), worldPosition));
		setChanged();
	}

	private Direction.Axis getAxis(){
		if(axisCache == null){
			BlockState state = getBlockState();
			if(state.getBlock() == ESBlocks.hopperFilter){
				axisCache = state.getValue(ESProperties.AXIS);
			}else{
				return Direction.Axis.Y;
			}
		}
		return axisCache;
	}

	@Override
	public void setBlockState(BlockState state){
		super.setBlockState(state);
		axisCache = null;
		if(passedHandlerNeg != null){
			passedHandlerNeg.invalidate();
			passedHandlerPos.invalidate();
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		nbt.put("filter", filter.save(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		filter = ItemStack.of(nbt.getCompound("filter"));
		filterItemsCache = null;
	}

	@Override
	public CompoundTag getUpdateTag(){
		CompoundTag nbt = super.getUpdateTag();
		saveAdditional(nbt);
		return nbt;
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		filter = ItemStack.of(nbt);
		filterItemsCache = null;
	}

	public boolean matchFilter(ItemStack query){
		if(filter.isEmpty()){
			return false;
		}

		if(filterItemsCache == null){
			if(filter.getItem() instanceof BlockItem && ((BlockItem) filter.getItem()).getBlock() instanceof ShulkerBoxBlock){
				CompoundTag nbt = filter.getOrCreateTag();
				NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
				//Loading the shulker box contents from NBT is slow, so we cache the result
				ContainerHelper.loadAllItems(nbt.getCompound("BlockEntityTag"), nonnulllist);
				filterItemsCache = nonnulllist.stream().map(ItemStack::getItem).collect(Collectors.toSet());
			}else{
				filterItemsCache = Set.of(filter.getItem());
			}
		}
		//The cache is a set because they are distinct and contains queries are constant time
		return filterItemsCache.contains(query.getItem());
	}

	private LazyOptional<IItemHandler> passedHandlerPos = null;
	private LazyOptional<IItemHandler> passedHandlerNeg = null;

	private void updatePassedOptionals(){
		if(passedHandlerPos == null || !passedHandlerPos.isPresent() && passedHandlerNeg == null || !passedHandlerNeg.isPresent()){
			passedHandlerNeg = LazyOptional.of(() -> new ProxyItemHandler(Direction.get(Direction.AxisDirection.NEGATIVE, getAxis())));
			passedHandlerPos = LazyOptional.of(() -> new ProxyItemHandler(Direction.get(Direction.AxisDirection.POSITIVE, getAxis())));
		}
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		if(passedHandlerNeg != null){
			passedHandlerNeg.invalidate();
			passedHandlerPos.invalidate();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side != null && side.getAxis() == getAxis()){
			updatePassedOptionals();
			return (LazyOptional<T>) (side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? passedHandlerPos : passedHandlerNeg);
		}

		return super.getCapability(cap, side);
	}

	private class ProxyItemHandler implements IItemHandler{

		private final Direction side;
		private LazyOptional<IItemHandler> src = LazyOptional.empty();

		private ProxyItemHandler(Direction side){
			this.side = side;
		}

		@Nullable
		private IItemHandler getHandler(){
			if(src.isPresent()){
				return src.orElseThrow(NullPointerException::new);
			}else{
				BlockPos checkPos = worldPosition.relative(side.getOpposite());
				BlockEntity checkTE = level.getBlockEntity(checkPos);

				if(checkTE != null){
					src = checkTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
					if(src.isPresent()){
						return src.orElseThrow(NullPointerException::new);
					}
				}

				BlockState checkState = level.getBlockState(checkPos);

				if(checkState.getBlock() instanceof WorldlyContainerHolder){
					//As the contract for ISidedInventoryProvider is poorly defined (there being only 1 vanilla example), we can't safely cache the result
					WorldlyContainer inv = ((WorldlyContainerHolder) checkState.getBlock()).getContainer(checkState, level, checkPos);
					return new InvWrapper(inv);
				}

				return null;
			}
		}

		@Override
		public int getSlots(){
			IItemHandler handler = getHandler();
			return handler == null ? 0 : handler.getSlots();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			IItemHandler handler = getHandler();
			return handler == null ? ItemStack.EMPTY : handler.getStackInSlot(slot);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
			IItemHandler handler = getHandler();
			if(handler != null && matchFilter(stack)){
				return handler.insertItem(slot, stack, simulate);
			}
			return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			IItemHandler handler = getHandler();
			if(handler != null && matchFilter(getStackInSlot(slot))){
				return handler.extractItem(slot, amount, simulate);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			IItemHandler handler = getHandler();
			return handler == null ? 0 : handler.getSlotLimit(slot);
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			IItemHandler handler = getHandler();
			return handler != null && matchFilter(stack) && handler.isItemValid(slot, stack);
		}
	}
}
