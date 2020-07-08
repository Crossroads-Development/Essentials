package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(Essentials.MODID)
public class BasicFluidSplitterTileEntity extends AbstractSplitterTE{

	@ObjectHolder("basic_fluid_splitter")
	private static TileEntityType<BasicFluidSplitterTileEntity> TYPE = null;

	public static final int[] MODES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	private final FluidStack[] inventory = new FluidStack[] {FluidStack.EMPTY, FluidStack.EMPTY};
	private static final int CAPACITY = 4000;

	public BasicFluidSplitterTileEntity(TileEntityType<? extends AbstractSplitterTE> type){
		super(type);
	}

	public BasicFluidSplitterTileEntity(){
		this(TYPE);
	}

	@Override
	protected int[] getModes(){
		return MODES;
	}

	@Override
	public void updateContainingBlockInfo(){
		super.updateContainingBlockInfo();
		primaryOpt.invalidate();
		secondaryOpt.invalidate();
		inOpt.invalidate();
		primaryOpt = LazyOptional.of(() -> new OutFluidHandler(1));
		secondaryOpt = LazyOptional.of(() -> new OutFluidHandler(0));
		inOpt = LazyOptional.of(InHandler::new);
	}

	@Override
	public void tick(){
		if(endPos[0] == null || endPos[1] == null){
			refreshCache();
		}

		Direction dir = getFacing();
		for(int i = 0; i < 2; i++){
			inventory[i] = AbstractShifterTileEntity.ejectFluid(world, endPos[i], i == 0 ? dir : dir.getOpposite(), inventory[i]);
		}
		markDirty();
	}

	@Override
	public void remove(){
		super.remove();
		primaryOpt.invalidate();
		secondaryOpt.invalidate();
		inOpt.invalidate();
	}

	private LazyOptional<IFluidHandler> primaryOpt = LazyOptional.of(() -> new OutFluidHandler(1));
	private LazyOptional<IFluidHandler> secondaryOpt = LazyOptional.of(() -> new OutFluidHandler(0));
	private LazyOptional<IFluidHandler> inOpt = LazyOptional.of(InHandler::new);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
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
				inventory[i].writeToNBT(inner);
				nbt.put("inv_" + i, inner);
			}
		}
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);

		//The way this block saves to nbt was changed in 2.2.0, and a "type" of 1 means the encoding is the new version, while 0 mean old version
		if(nbt.getByte("type") == 1){
			mode = nbt.getInt("mode");
		}else{
			mode = 3 + 3 * nbt.getInt("mode");
		}

		transfered = nbt.getInt("transfered");
		for(int i = 0; i < 2; i++){
			inventory[i] = FluidStack.loadFluidStackFromNBT(nbt.getCompound("inv_" + i));
		}
	}

	@Override
	public int getBase(){
		return 12;
	}

	private int transfered = 0;

	private class InHandler implements IFluidHandler{

		@Override
		public int getTanks(){
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank){
			return FluidStack.EMPTY;
		}

		@Override
		public int getTankCapacity(int tank){
			return CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
			return tank == 0;
		}

		@Override
		public int fill(FluidStack stack, FluidAction action){
			if(stack.isEmpty()){
				return 0;
			}
			
			//Ensure we are allowed to accept
			if(!inventory[0].isEmpty() && !BlockUtil.sameFluid(stack, inventory[0]) || !inventory[1].isEmpty() && !BlockUtil.sameFluid(stack, inventory[1])){
				return 0;
			}

			int numerator = getActualMode();
			int denominator = getBase();

			int accepted;//How many total qty we can accept
			if(numerator == 0){
				accepted = CAPACITY - inventory[1].getAmount();
			}else if(numerator == denominator){
				accepted = CAPACITY - inventory[0].getAmount();
			}else{
				accepted = denominator * (CAPACITY - inventory[0].getAmount()) / numerator;
				accepted = Math.min(accepted, denominator * (CAPACITY - inventory[1].getAmount()) / (denominator - numerator));
			}
			accepted = Math.max(0, Math.min(stack.getAmount(), accepted));//Sanity checks/bounding

			int goDown = numerator * (accepted / denominator);//Basic portion, with no need to interact with transferred

			//Tracking of individual remainder with regards to history
			int remainder = accepted % denominator;
			if(transfered < numerator){
				goDown += Math.min(numerator - transfered + Math.min((remainder + transfered) % denominator, numerator), remainder);
			}

			int goUp = accepted - goDown;
			

			if(action.execute() && accepted != 0){
				if(inventory[0].isEmpty()){
					inventory[0] = stack.copy();
					inventory[0].setAmount(goDown);
				}else{
					inventory[0].grow(goDown);
				}

				if(inventory[1].isEmpty()){
					inventory[1] = stack.copy();
					inventory[1].setAmount(goUp);
				}else{
					inventory[1].grow(goUp);
				}
				transfered += accepted % denominator;
				transfered %= denominator;
			}

			return Math.max(accepted, 0);
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, FluidAction action){
			return FluidStack.EMPTY;
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, FluidAction action){
			return FluidStack.EMPTY;
		}
	}

	protected class OutFluidHandler implements IFluidHandler{

		private final int index;

		private OutFluidHandler(int index){
			this.index = index;
		}

		@Override
		public int getTanks(){
			return 1;
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank){
			return inventory[index];
		}

		@Override
		public int getTankCapacity(int tank){
			return CAPACITY;
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack){
			return false;
		}

		@Override
		public int fill(FluidStack resource, FluidAction action){
			return 0;
		}

		@Nonnull
		@Override
		public FluidStack drain(FluidStack resource, FluidAction action){
			if(BlockUtil.sameFluid(resource, inventory[index])){
				int drained = Math.min(resource.getAmount(), inventory[index].getAmount());
				if(drained > 0){
					FluidStack out = inventory[index].copy();
					out.setAmount(drained);
					if(action.execute()){
						inventory[index].shrink(drained);
						markDirty();
					}
					return out;
				}
			}

			return FluidStack.EMPTY;
		}

		@Nonnull
		@Override
		public FluidStack drain(int maxDrain, FluidAction action){
			int drained = Math.min(maxDrain, inventory[index].getAmount());
			if(drained > 0){
				FluidStack out = inventory[index].copy();
				out.setAmount(drained);
				if(action.execute()){
					inventory[index].shrink(drained);
					markDirty();
				}
				return out;
			}

			return FluidStack.EMPTY;
		}
	}
} 
