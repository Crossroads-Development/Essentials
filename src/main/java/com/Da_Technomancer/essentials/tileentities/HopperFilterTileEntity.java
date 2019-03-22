package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendSlotFilterToClient;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class HopperFilterTileEntity extends TileEntity implements INBTReceiver{

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
		return oldState.getBlock() == newState.getBlock();
	}

	private EnumFacing.Axis axisCache = null;
	private ItemStack filter = ItemStack.EMPTY;

	public ItemStack getFilter(){
		return filter;
	}

	public void setFilter(ItemStack filter){
		this.filter = filter;
		EssentialsPackets.network.sendToAllAround(new SendSlotFilterToClient(filter.writeToNBT(new NBTTagCompound()), pos), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));
		markDirty();
	}

	private EnumFacing.Axis getAxis(){
		if(axisCache == null){
			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() == EssentialsBlocks.hopperFilter){
				axisCache = state.getValue(EssentialsProperties.AXIS);
			}else{
				return EnumFacing.Axis.Y;
			}
		}
		return axisCache;
	}

	public void clearCache(){
		axisCache = null;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		filter.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		filter = new ItemStack(nbt);
	}

	@Override
	public NBTTagCompound getUpdateTag(){
		return writeToNBT(super.getUpdateTag());
	}

	@Override
	public void receiveNBT(NBTTagCompound nbt){
		filter = new ItemStack(nbt);
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side.getAxis() == getAxis()){
			TileEntity te = world.getTileEntity(pos.offset(side.getOpposite()));
			return te != null && te.hasCapability(cap, side);
		}

		return super.hasCapability(cap, side);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> cap, EnumFacing side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && side.getAxis() == getAxis()){
			TileEntity te = world.getTileEntity(pos.offset(side.getOpposite()));
			IItemHandler srcHandler;
			if(te != null && (srcHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) != null){
				return (T) new ProxyItemHandler(srcHandler);
			}
		}

		return super.getCapability(cap, side);
	}

	private static boolean matchFilter(ItemStack query, ItemStack filt){
		if(filt.isEmpty()){
			return true;
		}

		NBTTagCompound nbt;
		if(filt.getItem() instanceof ItemShulkerBox && (nbt = filt.getTagCompound()) != null && (nbt = nbt.getCompoundTag("BlockEntityTag")).hasKey("Items", 9)){
			NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(nbt, nonnulllist);

			for(ItemStack singleFilt : nonnulllist){
				if(!singleFilt.isEmpty() && matchFilter(query, singleFilt)){
					return true;
				}
			}
			return false;
		}
		return query.getItem() == filt.getItem() && query.getMetadata() == filt.getMetadata();
	}

	private class ProxyItemHandler implements IItemHandler{

		private final IItemHandler src;

		private ProxyItemHandler(IItemHandler src){
			this.src = src;
		}

		@Override
		public int getSlots(){
			return src.getSlots();
		}

		@Nonnull
		@Override
		public ItemStack getStackInSlot(int slot){
			return src.getStackInSlot(slot);
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate){
			if(matchFilter(stack, filter)){
				return src.insertItem(slot, stack, simulate);
			}
			return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(matchFilter(getStackInSlot(slot), filter)){
				return src.extractItem(slot, amount, simulate);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return src.getSlotLimit(slot);
		}
	}
}
