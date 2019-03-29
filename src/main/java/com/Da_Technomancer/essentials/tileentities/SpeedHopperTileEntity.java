package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(Essentials.MODID)
public class SpeedHopperTileEntity extends SortingHopperTileEntity{

	@ObjectHolder("speed_hopper")
	private static TileEntityType<SpeedHopperTileEntity> TYPE = null;

	public SpeedHopperTileEntity(){
		super(TYPE);
		handler = new SpeedItemHandler();
	}

	@Override
	public ITextComponent getName(){
		return new TextComponentTranslation( "container.speed_hopper");
	}

	@Override
	protected int transferQuantity(){
		return 64;
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
				return inventory[slot].split(removed);
			}

			ItemStack out = inventory[slot].copy();
			out.setCount(removed);
			return out;
		}
	}
}
