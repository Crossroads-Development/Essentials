package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.FluidSlotManager;
import com.Da_Technomancer.essentials.api.IFluidSlotTE;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.fluidShifter;

public class FluidShifterTileEntity extends AbstractShifterTileEntity implements IFluidSlotTE{

	public static final BlockEntityType<FluidShifterTileEntity> TYPE = ESTileEntity.createType(FluidShifterTileEntity::new, fluidShifter);

	private static final int CAPACITY = 4_000;

	private FluidSlotManager fluidManager;
	private FluidStack fluid = FluidStack.EMPTY;

	public FluidSlotManager getFluidManager(){
		if(fluidManager == null){
			fluidManager = new FluidSlotManager(fluid, CAPACITY);
			//fluidManager.markChanged();
		}
		return fluidManager;
	}

	public FluidShifterTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	public void serverTick(){
		if(endPos == null){
			refreshCache();
		}

		FluidStack remaining = AbstractShifterTileEntity.ejectFluid(level, endPos, getFacing(), fluid);
		if(remaining.getAmount() != fluid.getAmount()){
			fluid = remaining;
			getFluidManager().updateState(fluid);
			setChanged();
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		nbt.put("fluid", fluid.writeToNBT(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluid"));
	}

	@Override
	public CompoundTag getUpdateTag(){
		CompoundTag nbt = super.getUpdateTag();
		saveAdditional(nbt);
		return nbt;
	}

	@Override
	public void setRemoved(){
		super.setRemoved();
		invOptional.invalidate();
	}

	private LazyOptional<IFluidHandler> invOptional = LazyOptional.of(FluidHandler::new);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return (LazyOptional<T>) invOptional;
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.fluid_shifter");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player){
		return new FluidShifterContainer(id, playerInventory, worldPosition);
	}

	@Override
	public IFluidHandler getFluidHandler(){
		return invOptional.orElseGet(FluidHandler::new);
	}

//	@Override
//	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
//		getFluidManager().handlePacket(nbt);
//		fluid = getFluidManager().getStack();
//	}

	private class FluidHandler implements IFluidHandler{

		@Override
		public int getTanks(){
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank){
			return fluid;
		}

		@Override
		public int getTankCapacity(int tank){
			return CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
			return true;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action){
			if((fluid.isEmpty() || BlockUtil.sameFluid(fluid, resource)) && !resource.isEmpty()){
				int filled = Math.min(CAPACITY - fluid.getAmount(), resource.getAmount());
				if(filled > 0 && action.execute()){
					if(fluid.isEmpty()){
						fluid = resource.copy();
						fluid.setAmount(filled);
					}else{
						fluid.grow(filled);
					}
					setChanged();
					getFluidManager().updateState(fluid);
				}
				return filled;
			}

			return 0;
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, FluidAction action){
			if(BlockUtil.sameFluid(fluid, resource)){
				int drained = Math.min(resource.getAmount(), fluid.getAmount());
				FluidStack drainFluid = drained == 0 ? FluidStack.EMPTY : fluid.copy();
				if(!drainFluid.isEmpty()){
					drainFluid.setAmount(drained);
				}

				if(drained > 0 && action.execute()){
					fluid.shrink(drained);
					setChanged();
					getFluidManager().updateState(fluid);
				}
				return drainFluid;
			}

			return FluidStack.EMPTY;
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, FluidAction action){
			int drained = Math.min(maxDrain, fluid.getAmount());
			FluidStack drainFluid = drained == 0 ? FluidStack.EMPTY : fluid.copy();
			if(!drainFluid.isEmpty()){
				drainFluid.setAmount(drained);
			}

			if(drained > 0 && action.execute()){
				fluid.shrink(drained);
				getFluidManager().updateState(fluid);
				setChanged();
			}

			return drainFluid;
		}
	}
}
