package com.Da_Technomancer.essentials.tileentities;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class SpeedHopperTileEntity extends SortingHopperTileEntity{

	public SpeedHopperTileEntity(){
		super();
		handler = new SpeedItemHandler();
	}

	@Override
	public String getName(){
		return "container.speed_hopper";
	}

	@Override
	protected boolean transferItemsOut(){
		EnumFacing facing = getDir();
		TileEntity te = world.getTileEntity(pos.offset(facing));

		//Insertion via IItemHandler
		if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())){
			IItemHandler otherHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
			for(int i = 0; i < getSizeInventory(); i++){
				ItemStack stackInSlot = getStackInSlot(i);
				if(!stackInSlot.isEmpty()){
					ItemStack insert = stackInSlot.copy();
					//insert.setCount(1);
					ItemStack newStack = ItemHandlerHelper.insertItem(otherHandler, insert, true);
					if(newStack.getCount() < insert.getCount()){
						ItemHandlerHelper.insertItem(otherHandler, decrStackSize(i, insert.getCount() - newStack.getCount()), false);
						markDirty();
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	protected boolean transferItemsIn(){
		TileEntity fromTE = world.getTileEntity(pos.offset(EnumFacing.UP));

		//Transfer from IItemHandler
		if(fromTE != null && fromTE.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)){
			IItemHandler otherHandler = fromTE.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);

			for (int i = 0; i < otherHandler.getSlots(); i++){
				ItemStack extractItem = otherHandler.extractItem(i, 64, true);
				if (!extractItem.isEmpty()){
					for (int j = 0; j < getSizeInventory(); j++){
						ItemStack uninserted = handler.insertItem(j, extractItem, false);
						if(uninserted.getCount() < extractItem.getCount()){
							otherHandler.extractItem(i, extractItem.getCount() - uninserted.getCount(), false);
							return true;
						}
					}
				}
			}

			return false;
		}else{
			boolean changed = false;

			//Suck up dropped items
			for(EntityItem entityitem : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.getX(), pos.getY() + 0.5D, pos.getZ(), pos.getX() + 1, pos.getY() + 2D, pos.getZ() + 1), EntitySelectors.IS_ALIVE)){
				if(entityitem == null){
					continue;
				}

				ItemStack stack = entityitem.getItem().copy();
				ItemStack remain = stack;

				for(int i = 0; i < 5; i++){
					remain = handler.insertItem(i, remain, false);
					if(remain.isEmpty()){
						break;
					}
				}

				if(remain.isEmpty()){
					entityitem.setDead();
					changed = true;
				}else if(remain.getCount() != stack.getCount()){
					entityitem.setItem(remain);
					changed = true;
				}
			}
			return changed;
		}
	}


	private class SpeedItemHandler extends ItemHandler{

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(amount <= 0 || slot > 4){
				return ItemStack.EMPTY;
			}

			int removed = Math.min(amount, inventory[slot].getCount());

			if(!simulate){
				markDirty();
				return inventory[slot].splitStack(removed);
			}

			ItemStack out = inventory[slot].copy();
			out.setCount(removed);
			return out;
		}
	}
}
