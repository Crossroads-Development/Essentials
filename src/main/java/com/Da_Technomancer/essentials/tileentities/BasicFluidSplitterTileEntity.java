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

	private final FluidStack[] inventory = new FluidStack[] {FluidStack.EMPTY, FluidStack.EMPTY};
	private static final int CAPACITY = 4000;

	public BasicFluidSplitterTileEntity(TileEntityType<? extends AbstractSplitterTE> type){
		super(type);
	}

	public BasicFluidSplitterTileEntity(){
		this(TYPE);
	}

	@Override
	public void clearCache(){
		super.clearCache();
		primaryOpt.invalidate();
		secondaryOpt.invalidate();
		inOpt.invalidate();
		primaryOpt = LazyOptional.of(() -> new OutFluidHandler(1));
		secondaryOpt = LazyOptional.of(() -> new OutFluidHandler(0));
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
			inventory[i] = AbstractShifterTileEntity.ejectFluid(level, endPos[i], i == 0 ? dir : dir.getOpposite(), inventory[i]);
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
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.putByte("type", (byte) 1);//Version number for the nbt data
		nbt.putInt("mode", mode);
		nbt.putInt("transferred", transferred);
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
			inventory[i] = FluidStack.loadFluidStackFromNBT(nbt.getCompound("inv_" + i));
		}
	}

	private int transferred = 0;

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

			int numerator = getMode();
			SplitDistribution distribution = getDistribution();
			int denominator = distribution.base;

			int accepted;//How many total qty we accepted
			int goDown;//How many of accepted went down vs up
			int spaceDown = CAPACITY - inventory[0].getAmount();
			int spaceUp = CAPACITY - inventory[1].getAmount();
			if(numerator == 0){
				accepted = Math.min(spaceUp, stack.getAmount());
				goDown = 0;
			}else if(numerator == denominator){
				accepted = Math.min(spaceDown, stack.getAmount());
				goDown = accepted;
			}else{
				//Calculate the split for the amount divisible by our base first
				int baseQty = stack.getAmount() - stack.getAmount() % denominator;
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
				int remainder = stack.getAmount() - accepted;
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

			//Actually move the fluid

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
				transferred += accepted;
				transferred %= denominator;
			}

			return accepted;
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
						setChanged();
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
					setChanged();
				}
				return out;
			}

			return FluidStack.EMPTY;
		}
	}
} 
