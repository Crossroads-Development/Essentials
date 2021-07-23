package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE.SplitDistribution;

@ObjectHolder(Essentials.MODID)
public class FluidSplitterBlockEntity extends BasicFluidSplitterBlockEntity{

	@ObjectHolder("fluid_splitter")
	private static BlockEntityType<FluidSplitterBlockEntity> TYPE = null;

	public int redstone;

	public FluidSplitterBlockEntity(){
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
	public int getMode(){
		return redstone;
	}

	@Override
	public SplitDistribution getDistribution(){
		return SplitDistribution.FIFTEEN;
	}
} 
