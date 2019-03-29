package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.gui.CrudeFluidBar;
import com.Da_Technomancer.essentials.gui.EssentialsGuiHandler;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

@ObjectHolder(Essentials.MODID)
public class FluidShifterTileEntity extends TileEntity implements ITickable, IInventory, IInteractionObject{

	@ObjectHolder("fluid_splitter")
	private static TileEntityType<FluidShifterTileEntity> TYPE = null;

	private FluidStack inventory = null;
	private BlockPos endPos = null;
	public static final int CAPACITY = 4_000;

	private EnumFacing facing = null;

	public FluidShifterTileEntity(){
		super(TYPE);
	}

	private EnumFacing getFacing(){
		if(facing == null){
			IBlockState state = world.getBlockState(pos);
			if(!state.has(EssentialsProperties.FACING)){
				return EnumFacing.DOWN;
			}
			facing = state.get(EssentialsProperties.FACING);
		}
		return facing;
	}

	@Override
	public void tick(){
		if(world.isRemote){
			return;
		}

		if(endPos == null){
			refreshCache();
		}

		if(inventory == null){
			return;
		}

		TileEntity outputTE = world.getTileEntity(endPos);
		EnumFacing dir = getFacing();
		LazyOptional<IFluidHandler> outHandlerCon;
		if(outputTE != null && (outHandlerCon = outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite())).isPresent()){
			IFluidHandler outHandler = outHandlerCon.orElseThrow(NullPointerException::new);
			int filled = outHandler.fill(inventory, true);
			if(filled != 0){
				if(inventory.amount == filled){
					inventory = null;
				}else{
					inventory.amount -= filled;
				}
				markDirty();
			}
		}
	}

	public void refreshCache(){
		facing = null;
		EnumFacing dir = getFacing();
		int extension = 1;
		int maxChutes = EssentialsConfig.itemChuteRange.get();

		for(; extension <= maxChutes; extension++){
			IBlockState target = world.getBlockState(pos.offset(dir, extension));
			if(target.getBlock() != EssentialsBlocks.itemChute || target.get(EssentialsProperties.AXIS) != dir.getAxis()){
				break;
			}
		}

		endPos = pos.offset(dir, extension);
	}

	@Override
	public NBTTagCompound write(NBTTagCompound nbt){
		super.write(nbt);

		if(inventory != null){
			nbt.put("inv", inventory.writeToNBT(new NBTTagCompound()));
		}
		return nbt;
	}

	@Override
	public void read(NBTTagCompound nbt){
		super.read(nbt);

		if(nbt.contains("inv")){
			inventory = FluidStack.loadFluidStackFromNBT(nbt.getCompound("inv"));
		}
	}

	private final IFluidHandler handler = new FluidHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, EnumFacing facing){
		if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return LazyOptional.of(() -> (T) handler);
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn){
		return new FluidShifterContainer(playerInventory, this);
	}

	@Override
	public String getGuiID(){
		return EssentialsGuiHandler.FLUID_SHIFTER_GUI;
	}

	private class FluidHandler implements IFluidHandler{

		@Override
		public IFluidTankProperties[] getTankProperties(){
			return new IFluidTankProperties[] {new FluidTankProperties(inventory, CAPACITY)};
		}


		@Override
		public int fill(FluidStack resource, boolean doFill){
			if(resource != null && (inventory == null || inventory.isFluidEqual(resource))){
				int change = Math.min(CAPACITY - (inventory == null ? 0 : inventory.amount), resource.amount);
				if(doFill){
					int prevAmount = inventory == null ? 0 : inventory.amount;
					inventory = resource.copy();
					inventory.amount = prevAmount + change;
					markDirty();
				}
				return change;
			}

			return 0;
		}

		@Nullable
		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain){
			if(resource != null && resource.isFluidEqual(inventory)){
				int change = Math.min(inventory.amount, resource.amount);

				if(doDrain){
					inventory.amount -= change;
					if(inventory.amount == 0){
						inventory = null;
					}
					markDirty();
				}
				FluidStack out = resource.copy();
				out.amount = change;
				return out;
			}

			return null;
		}

		@Nullable
		@Override
		public FluidStack drain(int maxDrain, boolean doDrain){
			if(maxDrain != 0 && inventory != null){
				int change = Math.min(inventory.amount, maxDrain);
				FluidStack content = inventory.copy();
				content.amount = change;

				if(doDrain){
					inventory.amount -= change;
					if(inventory.amount == 0){
						inventory = null;
					}
					markDirty();
				}

				return content;
			}

			return null;
		}
	}

	@Override
	public ITextComponent getName(){
		return new TextComponentTranslation("container.fluid_shifter");
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Nullable
	@Override
	public ITextComponent getCustomName(){
		return null;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player){
		return world.getTileEntity(pos) == this && player.getDistanceSq(pos.add(0.5, 0.5, 0.5)) <= 64;
	}

	@Override
	public void openInventory(EntityPlayer player){

	}

	@Override
	public void closeInventory(EntityPlayer player){

	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack){
		return false;
	}

	@Override
	public int getSizeInventory(){
		return 0;
	}

	@Override
	public boolean isEmpty(){
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index){
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int index, int count){
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStackFromSlot(int index){
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack){

	}

	@Override
	public int getInventoryStackLimit(){
		return 0;
	}

	@Override
	public void clear(){

	}

	private short[] clientFluids = new short[2];

	@Override
	public int getField(int id){
		if(id < 2){
			if(world.isRemote){
				return clientFluids[id];
			}
			return CrudeFluidBar.fluidToPacket(inventory)[id];
		}
		return 0;
	}

	@Override
	public void setField(int id, int value){
		if(id < 2){
			clientFluids[id] = (short) value;
		}
	}

	@Override
	public int getFieldCount(){
		return 2;
	}
}
