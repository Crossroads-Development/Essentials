package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class ItemSplitterTileEntity extends BasicItemSplitterTileEntity{

	@ObjectHolder("item_splitter")
	private static TileEntityType<ItemSplitterTileEntity> TYPE = null;

	public int redstone;

	public ItemSplitterTileEntity(){
		super(TYPE);
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putInt("reds", redstone);
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		redstone = nbt.getInt("reds");
	}

	@Override
	protected int getActualMode(){
		return redstone;
	}

	@Override
	public int getBase(){
		return 15;
	}
} 
