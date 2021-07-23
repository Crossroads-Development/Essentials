package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE.SplitDistribution;

@ObjectHolder(Essentials.MODID)
public class FluidSplitterTileEntity extends BasicFluidSplitterTileEntity{

	@ObjectHolder("fluid_splitter")
	private static BlockEntityType<FluidSplitterTileEntity> TYPE = null;

	public int redstone;

	public FluidSplitterTileEntity(){
		super(TYPE);
	}

	@Override
	public CompoundTag save(CompoundTag nbt){
		super.save(nbt);
		nbt.putInt("reds", redstone);
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundTag nbt){
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
