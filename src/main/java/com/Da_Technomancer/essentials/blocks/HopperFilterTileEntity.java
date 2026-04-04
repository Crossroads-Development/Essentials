package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.IItemCapable;
import com.Da_Technomancer.essentials.api.packets.INBTReceiver;
import com.Da_Technomancer.essentials.api.packets.SendNBTToTE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.hopperFilter;

public class HopperFilterTileEntity extends BlockEntity implements INBTReceiver, IItemCapable{

	public static final BlockEntityType<HopperFilterTileEntity> TYPE = ESTileEntity.createType(HopperFilterTileEntity::new, hopperFilter);

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
		BlockUtil.sendClientPacketAround(level, worldPosition, new SendNBTToTE(BlockUtil.stackToNBT(filter, level.registryAccess()), worldPosition));
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
		level.invalidateCapabilities(worldPosition);
		passedHandlerPos = passedHandlerNeg = null;
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.saveAdditional(nbt, registries);
		nbt.put("filter", BlockUtil.stackToNBT(filter, registries));
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.loadAdditional(nbt, registries);
		filter = BlockUtil.nbtToItemStack(nbt.getCompound("filter"), registries);
		filterItemsCache = null;
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries){
		CompoundTag nbt = super.getUpdateTag(registries);
		saveAdditional(nbt, registries);
		return nbt;
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		filter = BlockUtil.nbtToItemStack(nbt, level.registryAccess());
		filterItemsCache = null;
	}

	public boolean matchFilter(ItemStack query){
		if(filter.isEmpty()){
			return false;
		}

		if(filterItemsCache == null){
			if(filter.getItem() instanceof BundleItem){
				BundleContents bundleContents = filter.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
				filterItemsCache = bundleContents.itemCopyStream().map(ItemStack::getItem).collect(Collectors.toSet());
			}else if(filter.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock){
				//Loading the shulker box contents from NBT is slow, so we cache the result
				filterItemsCache = filter.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyStream().map(ItemStack::getItem).collect(Collectors.toSet());
			}else{
				filterItemsCache = Set.of(filter.getItem());
			}
		}
		//The cache is a set because they are distinct and contains queries are constant time
		return filterItemsCache.contains(query.getItem());
	}

	private IItemHandler passedHandlerPos = null;
	private IItemHandler passedHandlerNeg = null;

	@Nullable
	@Override
	public IItemHandler getItemHandler(Direction dir){
		if(dir != null && dir.getAxis() == getAxis()){
			if(passedHandlerPos == null || passedHandlerNeg == null){
				passedHandlerNeg = new ProxyItemHandler(Direction.get(Direction.AxisDirection.NEGATIVE, getAxis()));
				passedHandlerPos = new ProxyItemHandler(Direction.get(Direction.AxisDirection.POSITIVE, getAxis()));
			}
			return dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? passedHandlerPos : passedHandlerNeg;
		}
		return null;
	}

	public class ProxyItemHandler implements IItemHandler{

		private final Direction side;
		private final BlockCapabilityCache<IItemHandler, Direction> src;

		private ProxyItemHandler(Direction side){
			this.side = side;
			BlockPos checkPos = worldPosition.relative(side.getOpposite());
			src = level instanceof ServerLevel sLevel ? BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, sLevel, checkPos, side) : null;
		}

		public boolean isNakedHandler(){
			return getHandler() == null;
		}

		@Nullable
		private IItemHandler getHandler(){
			return src == null ? null : src.getCapability();
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
