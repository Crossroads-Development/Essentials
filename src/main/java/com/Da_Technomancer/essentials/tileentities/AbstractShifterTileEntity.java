package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public abstract class AbstractShifterTileEntity extends BlockEntity implements ITickableTileEntity, MenuProvider{

	private Direction facing = null;
	protected BlockPos endPos = null;

	protected <T extends AbstractShifterTileEntity> AbstractShifterTileEntity(BlockEntityType<T> type, BlockPos pos, BlockState state){
		super(type, pos, state);
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
	public void setBlockState(BlockState state){
		super.setBlockState(state);
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

	public static ItemStack ejectItem(Level world, BlockPos outputPos, Direction fromSide, ItemStack stack, @Nullable LazyOptional<IItemHandler> outputHandlerCache){
		if(stack.isEmpty()){
			return ItemStack.EMPTY;
		}
		
		//Capability item handlers
		IItemHandler handler = null;

		//Use cache if possible
		if(outputHandlerCache != null && outputHandlerCache.isPresent()){
			handler = outputHandlerCache.orElseThrow(NullPointerException::new);
		}

		if(handler == null){
			//Null means no cache, check independently
			handler = SortingHopperTileEntity.getHandlerAtPosition(world, outputPos, fromSide, null);
		}

		if(handler != null){
			//Found an item handler. Interact with it
			for(int i = 0; i < handler.getSlots(); i++){
				ItemStack outStack = handler.insertItem(i, stack, false);
				if(outStack.getCount() != stack.getCount()){
					return outStack;
				}
			}
			return stack;
		}

		//No item handler found
		//Drop the item in the world
		ItemEntity ent = new ItemEntity(world, outputPos.getX() + 0.5D, outputPos.getY() + 0.5D, outputPos.getZ() + 0.5D, stack);
		ent.setDeltaMovement(Vec3.ZERO);
		world.addFreshEntity(ent);
		return ItemStack.EMPTY;
	}

	public static FluidStack ejectFluid(Level world, BlockPos pos, Direction fromSide, FluidStack stack){
		if(stack.isEmpty()){
			return FluidStack.EMPTY;
		}

		BlockEntity outputTE = world.getBlockEntity(pos);
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
