package com.Da_Technomancer.essentials.tileentities;

import net.minecraft.nbt.NBTTagCompound;

public class ItemSplitterTileEntity extends BasicItemSplitterTileEntity{

	public ItemSplitterTileEntity(){
		super();
	}

	public int redstone;

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		nbt.setInteger("reds", redstone);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		redstone = nbt.getInteger("reds");
	}

	@Override
	protected int getPortion(){
		return redstone;
	}

	@Override
	protected int getBase(){
		return 15;
	}
} 
