package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.tileentity.BlockEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class SpeedHopperBlockEntity extends SortingHopperBlockEntity{

	@ObjectHolder("speed_hopper")
	private static BlockEntityType<SpeedHopperBlockEntity> TYPE = null;

	public SpeedHopperBlockEntity(){
		super(TYPE);
		//handler = new SpeedItemHandler();
	}


	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent( "container.speed_hopper");
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

			BlockEntity te = world.getBlockEntity(pos.offset(facing));
			LazyOptional<IItemHandler> otherCap;
			if(te != null && (otherCap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())).isPresent()){
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
