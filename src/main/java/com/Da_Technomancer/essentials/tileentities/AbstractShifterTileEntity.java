package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class AbstractShifterTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider{

	private Direction facing = null;
	protected BlockPos endPos = null;

	protected <T extends AbstractShifterTileEntity> AbstractShifterTileEntity(TileEntityType<T> type){
		super(type);
	}

	protected Direction getFacing(){
		if(facing == null){
			BlockState state = world.getBlockState(pos);
			if(!state.has(EssentialsProperties.FACING)){
				return Direction.DOWN;
			}
			facing = state.get(EssentialsProperties.FACING);
		}
		return facing;
	}

	public void refreshCache(){
		facing = null;
		Direction dir = getFacing();
		int extension;
		int maxChutes = EssentialsConfig.itemChuteRange.get();

		for(extension = 1; extension <= maxChutes; extension++){
			BlockState target = world.getBlockState(pos.offset(dir, extension));
			if(target.getBlock() != EssentialsBlocks.itemChute || target.get(EssentialsProperties.AXIS) != dir.getAxis()){
				break;
			}
		}

		endPos = pos.offset(dir, extension);
	}
	
	public static ItemStack ejectItem(World world,BlockPos pos, Direction fromSide, ItemStack stack){
		if(stack.isEmpty()){
			return ItemStack.EMPTY;
		}

		TileEntity outputTE = world.getTileEntity(pos);
		LazyOptional<IItemHandler> outputCap;
		if(outputTE != null && (outputCap = outputTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, fromSide.getOpposite())).isPresent()){
			IItemHandler outHandler = outputCap.orElseThrow(NullPointerException::new);
			for(int i = 0; i < outHandler.getSlots(); i++){
				ItemStack outStack = outHandler.insertItem(i, stack, false);
				if(outStack.getCount() != stack.getCount()){
					return outStack;
				}
			}
			return stack;
		}

		ItemEntity ent = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
		ent.setMotion(Vec3d.ZERO);
		world.addEntity(ent);
		return ItemStack.EMPTY;
	}
}
