package com.Da_Technomancer.essentials.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.speedHopper;

public class SpeedHopperTileEntity extends SortingHopperTileEntity{

	public static final BlockEntityType<SpeedHopperTileEntity> TYPE = ESTileEntity.createType(SpeedHopperTileEntity::new, speedHopper);

	public SpeedHopperTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
		//handler = new SpeedItemHandler();
	}


	@Override
	public Component getDisplayName(){
		return Component.translatable("container.speed_hopper");
	}

	@Override
	protected int transferQuantity(){
		return 64;
	}

/*
	private class SpeedItemHandler extends ItemHandler{

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(amount <= 0 || slot > 4){
				return ItemStack.EMPTY;
			}

			Direction facing = getDir();

			TileEntity te = world.getTileEntity(pos.offset(facing));
			LazyOptional<IItemHandler> otherCap;
			if(te != null && (otherCap = te.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite())).isPresent()){
				IItemHandler otherHandler = otherCap.orElseThrow(NullPointerException::new);
				int slots = otherHandler.getSlots();
				for(int i = 0; i < slots; i++){
					if(otherHandler.insertItem(i, inventory[slot], true).getCount() < inventory[slot].getCount()){
						return ItemStack.EMPTY;//Tbe special feature of the sorting hopper is that items can't be drawn from it unless the sorting hopper wouldn't be able to export it.
					}
				}
			}

			int removed = Math.min(amount, inventory[slot].getCount());

			if(!simulate){
				markDirty();
				return inventory[slot].split(removed);
			}

			ItemStack out = inventory[slot].copy();
			out.setCount(removed);
			return out;


			//

			if(amount <= 0 || slot > 4){
				return ItemStack.EMPTY;
			}

			int removed = Math.min(amount, inventory[slot].getCount());

			if(!simulate){
				markDirty();
				return inventory[slot].split(removed);
			}

			ItemStack out = inventory[slot].copy();
			out.setCount(removed);
			return out;
		}
	}*/
}
