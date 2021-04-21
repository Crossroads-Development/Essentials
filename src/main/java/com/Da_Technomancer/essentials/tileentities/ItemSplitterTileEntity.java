package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE.SplitDistribution;

@ObjectHolder(Essentials.MODID)
public class ItemSplitterTileEntity extends BasicItemSplitterTileEntity{

	@ObjectHolder("item_splitter")
	private static TileEntityType<ItemSplitterTileEntity> TYPE = null;

	public int redstone;

	public ItemSplitterTileEntity(){
		super(TYPE);
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.putInt("reds", redstone);
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);
		redstone = nbt.getInt("reds");
	}

	@Override
	public SplitDistribution getDistribution(){
		return SplitDistribution.FIFTEEN;
	}

	@Override
	public int getMode(){
		return redstone;
	}
} 
