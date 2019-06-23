package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.PlantType;

public class CandleLilyPad extends LilyPadBlock{

	protected CandleLilyPad(){
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0).sound(SoundType.PLANT).lightValue(14));
		String name = "candle_lilypad";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
	}

	@Override
	public PlantType getPlantType(IBlockReader world, BlockPos pos){
		return PlantType.Water;
	}
}
