package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import com.Da_Technomancer.essentials.gui.container.FluidSlotManager;
import com.Da_Technomancer.essentials.gui.container.IFluidSlotTE;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

@ObjectHolder(Essentials.MODID)
public class FluidShifterTileEntity extends AbstractShifterTileEntity implements IFluidSlotTE{

	@ObjectHolder("fluid_shifter")
	private static TileEntityType<FluidShifterTileEntity> TYPE = null;
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

	public FluidShifterTileEntity(){
		super(TYPE);
	}

	@Override
	public void tick(){
		if(level.isClientSide){
			return;
		}

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
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.put("fluid", fluid.writeToNBT(new CompoundNBT()));
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);
		fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluid"));
	}

	@Override
	public CompoundNBT getUpdateTag(){
		return save(super.getUpdateTag());
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
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.fluid_shifter");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player){
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
