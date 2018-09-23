package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.shared.IAxisHandler;
import com.Da_Technomancer.essentials.shared.IAxleHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemChutePortTileEntity extends TileEntity implements ITickable{

	private ItemStack inventory = ItemStack.EMPTY;
	private final double[] motionData = new double[4];
	@CapabilityInject(IAxleHandler.class)
	private static Capability<?> AXLE_HANDLER_CAPABILITY = null;

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
		return oldState.getBlock() != newState.getBlock();
	}
	
	@Override
	public void update(){
		if(world.isRemote){
			return;
		}

		if(isSpotInvalid() && !inventory.isEmpty()){
			world.spawnEntity(new EntityItem(world, pos.offset(world.getBlockState(pos).getValue(EssentialsProperties.FACING)).getX(), pos.getY(), pos.offset(world.getBlockState(pos).getValue(EssentialsProperties.FACING)).getZ(), inventory.copy()));
			inventory = ItemStack.EMPTY;
			markDirty();
		}else if(!inventory.isEmpty() && (!Essentials.hasCrossroads || !EssentialsConfig.getConfigBool(EssentialsConfig.itemChuteRotary, false) || Math.abs(motionData[1]) >= .5D) && output()){
			if(Essentials.hasCrossroads && EssentialsConfig.getConfigBool(EssentialsConfig.itemChuteRotary, true)){
				motionData[1] -= 0.5D * Math.signum(motionData[1]);
				markDirty();
			}
			inventory = ItemStack.EMPTY;
			markDirty();
		}
	}
	
	public void dropItems(){
		world.spawnEntity(new EntityItem(world, pos.offset(world.getBlockState(pos).getValue(EssentialsProperties.FACING)).getX(), pos.getY(), pos.offset(world.getBlockState(pos).getValue(EssentialsProperties.FACING)).getZ(), inventory.copy()));
		inventory = ItemStack.EMPTY;
		markDirty();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);

		if(!inventory.isEmpty()){
			nbt.setTag("inv", inventory.writeToNBT(new NBTTagCompound()));
		}
		for(int i = 0; i < 4; i++){
			nbt.setDouble("motion" + i, motionData[i]);
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);

		if(nbt.hasKey("inv")){
			inventory = new ItemStack(nbt.getCompoundTag("inv"));
		}
		for(int i = 0; i < 4; i++){
			motionData[i] = nbt.getDouble("motion" + i);
		}
	}

	private boolean isSpotInvalid(){
		Block block = world.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock();
		Block blockUp = world.getBlockState(pos.offset(EnumFacing.UP)).getBlock();
		return block == EssentialsBlocks.itemChutePort || block == EssentialsBlocks.itemChute || (blockUp != EssentialsBlocks.itemChutePort && blockUp != EssentialsBlocks.itemChute);
	}

	private boolean output(){
		BlockPos outputPos = null;
		EnumFacing outDir = null;

		for(int height = 1; height < 255 - pos.getY(); height++){
			IBlockState state = world.getBlockState(pos.offset(EnumFacing.UP, height));
			if(state.getBlock() == EssentialsBlocks.itemChutePort){
				outputPos = pos.offset(EnumFacing.UP, height);
				outDir = state.getValue(EssentialsProperties.FACING);
				break;
			}

			if(state.getBlock() != EssentialsBlocks.itemChute){
				return false;
			}
		}

		if(outputPos == null){
			return false;
		}

		TileEntity offsetTE = world.getTileEntity(outputPos.offset(outDir));
		if(offsetTE != null && offsetTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outDir.getOpposite())){
			IItemHandler outHandler = offsetTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, outDir.getOpposite());
			for(int i = 0; i < outHandler.getSlots(); i++){
				ItemStack outStack = outHandler.insertItem(i, inventory, false);
				if(outStack.isEmpty()){
					return true;
				}
			}
			return false;
		}

		EntityItem ent = new EntityItem(world, outputPos.offset(outDir).getX() + .5D, outputPos.offset(outDir).getY(), outputPos.offset(outDir).getZ() + .5D, inventory);
		ent.motionX = 0;
		ent.motionZ = 0;
		world.spawnEntity(ent);
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing facing){
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() != EssentialsBlocks.itemChutePort){
			return false;
		}

		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && (facing == null || facing == state.getValue(EssentialsProperties.FACING))){
			return true;
		}
		if(cap == AXLE_HANDLER_CAPABILITY && Essentials.hasCrossroads && EssentialsConfig.getConfigBool(EssentialsConfig.itemChuteRotary, true) && facing == state.getValue(EssentialsProperties.FACING).rotateAround(Axis.Y)){
			return true;
		}

		return super.hasCapability(cap, facing);
	}

	private Object axleHandler = null;
	private final IItemHandler handler = new InventoryHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing facing){
		IBlockState state = world.getBlockState(pos);
		if(state.getBlock() != EssentialsBlocks.itemChutePort){
			return super.getCapability(cap, facing);
		}

		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && (facing == null || facing == state.getValue(EssentialsProperties.FACING))){
			return (T) handler;
		}
		if(cap == AXLE_HANDLER_CAPABILITY && Essentials.hasCrossroads && EssentialsConfig.getConfigBool(EssentialsConfig.itemChuteRotary, true) && facing == state.getValue(EssentialsProperties.FACING).rotateAround(Axis.Y)){
			if(axleHandler == null){
				axleHandler = new AxleHandler();
			}
			return (T) axleHandler;
		}

		return super.getCapability(cap, facing);
	}

	private class AxleHandler implements IAxleHandler{
		@Override
		public double[] getMotionData(){
			return motionData;
		}

		private double rotRatio;
		private byte updateKey;

		@Override
		public void propogate(IAxisHandler masterIn, byte key, double rotRatioIn, double lastRadius){
			//If true, this has already been checked.
			if(key == updateKey || masterIn.addToList(this)){
				return;
			}

			rotRatio = rotRatioIn == 0 ? 1 : rotRatioIn;
			updateKey = key;
		}

		@Override
		public double getMoInertia(){
			return 8;
		}

		@Override
		public double getRotationRatio(){
			return rotRatio;
		}

		@Override
		public void addEnergy(double energy, boolean allowInvert, boolean absolute){
			if(allowInvert && absolute){
				motionData[1] += energy;
			}else if(allowInvert){
				motionData[1] += energy * Math.signum(motionData[1]);
			}else if(absolute){
				int sign = (int) Math.signum(motionData[1]);
				motionData[1] += energy;
				if(sign != 0 && Math.signum(motionData[1]) != sign){
					motionData[1] = 0;
				}
			}else{
				int sign = (int) Math.signum(motionData[1]);
				motionData[1] += energy * ((double) sign);
				if(Math.signum(motionData[1]) != sign){
					motionData[1] = 0;
				}
			}
			markDirty();
		}

		@Override
		public void markChanged(){
			markDirty();
		}

		@Override
		public boolean shouldManageAngle(){
			return false;
		}
	}

	private class InventoryHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			return slot == 0 ? inventory : ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot != 0 || stack.isEmpty() || !inventory.isEmpty()){
				return stack;
			}

			if(!simulate){
				inventory = stack.copy();
				inventory.setCount(1);
				markDirty();
			}

			ItemStack holder = stack.copy();
			holder.shrink(1);
			return holder;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return slot == 0 ? 1 : 0;
		}
	}
}
