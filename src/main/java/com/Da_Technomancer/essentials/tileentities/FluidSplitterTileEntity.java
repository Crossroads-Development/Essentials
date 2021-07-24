package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class FluidSplitterTileEntity extends BasicFluidSplitterTileEntity{

	@ObjectHolder("fluid_splitter")
	public static BlockEntityType<FluidSplitterTileEntity> TYPE = null;

	public int redstone;

	public FluidSplitterTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	public CompoundTag save(CompoundTag nbt){
		super.save(nbt);
		nbt.putInt("reds", redstone);
		return nbt;
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
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
