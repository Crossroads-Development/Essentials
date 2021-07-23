package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nullable;

public abstract class AbstractShifterTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider{

	private Direction facing = null;
	protected BlockPos endPos = null;

	protected <T extends AbstractShifterTileEntity> AbstractShifterTileEntity(TileEntityType<T> type){
		super(type);
	}

	protected Direction getFacing(){
		if(facing == null){
			BlockState state = getBlockState();
			if(!state.hasProperty(ESProperties.FACING)){//MCP note: has
				return Direction.DOWN;
			}
			facing = state.getValue(ESProperties.FACING);
		}
		return facing;
	}

	@Override
	public void clearCache(){
		super.clearCache();
		facing = null;
		refreshCache();
	}

	public void refreshCache(){
		Direction dir = getFacing();
		int extension;
		int maxChutes = ESConfig.itemChuteRange.get();

		for(extension = 1; extension <= maxChutes; extension++){
			BlockState target = level.getBlockState(worldPosition.relative(dir, extension));
			if(target.getBlock() != ESBlocks.itemChute || target.getValue(ESProperties.AXIS) != dir.getAxis()){
				break;
			}
		}

		endPos = worldPosition.relative(dir, extension);
	}
	
	public static ItemStack ejectItem(World world, BlockPos outputPos, Direction fromSide, ItemStack stack, @Nullable LazyOptional<IItemHandler> outputHandlerCache){
		if(stack.isEmpty()){
			return ItemStack.EMPTY;
		}

		IItemHandler handler = null;

		//Capability item handlers
		//Null means no cache, check independently
		if(outputHandlerCache == null){
			TileEntity outputTE = world.getBlockEntity(outputPos);
			LazyOptional<IItemHandler> outputCap;
			if(outputTE != null && (outputCap = outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, fromSide.getOpposite())).isPresent()){
				handler = outputCap.orElseThrow(NullPointerException::new);
			}
		}else if(outputHandlerCache.isPresent()){
			handler = outputHandlerCache.orElseThrow(NullPointerException::new);
		}

		//ISidedInventoryProvider
		if(handler == null){
			BlockState outputState = world.getBlockState(outputPos);
			if(outputState.getBlock() instanceof ISidedInventoryProvider){
				ISidedInventory inv = ((ISidedInventoryProvider) outputState.getBlock()).getContainer(outputState, world, outputPos);
				handler = new InvWrapper(inv);
			}
		}

		if(handler != null){
			for(int i = 0; i < handler.getSlots(); i++){
				ItemStack outStack = handler.insertItem(i, stack, false);
				if(outStack.getCount() != stack.getCount()){
					return outStack;
				}
			}
			return stack;
		}

		//Drop the item in the world
		ItemEntity ent = new ItemEntity(world, outputPos.getX() + 0.5D, outputPos.getY() + 0.5D, outputPos.getZ() + 0.5D, stack);
		ent.setDeltaMovement(Vector3d.ZERO);
		world.addFreshEntity(ent);
		return ItemStack.EMPTY;
	}

	public static FluidStack ejectFluid(World world, BlockPos pos, Direction fromSide, FluidStack stack){
		if(stack.isEmpty()){
			return FluidStack.EMPTY;
		}

		TileEntity outputTE = world.getBlockEntity(pos);
		LazyOptional<IFluidHandler> outHandlerCon;
		if(outputTE != null && (outHandlerCon = outputTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, fromSide.getOpposite())).isPresent()){
			IFluidHandler outHandler = outHandlerCon.orElseThrow(NullPointerException::new);
			int filled = outHandler.fill(stack, IFluidHandler.FluidAction.EXECUTE);
			FluidStack out = stack.copy();
			out.shrink(filled);
			return out;
		}

		return stack;
	}
}
