package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class FluidSplitterTileEntity extends BasicFluidSplitterTileEntity{

	@ObjectHolder("fluid_splitter")
	private static TileEntityType<FluidSplitterTileEntity> TYPE = null;

	public int redstone;

	public FluidSplitterTileEntity(){
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
	public int getMode(){
		return redstone;
	}

	@Override
	public SplitDistribution getDistribution(){
		return SplitDistribution.FIFTEEN;
	}
} 
