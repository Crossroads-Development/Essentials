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
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.common.PlantType;

public class CandleLilyPad extends LilyPadBlock{

	protected CandleLilyPad(){
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0).sound(SoundType.PLANT));
		String name = "candle_lilypad";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
	}

	@Override
	public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos){
		return 14;
	}

	@Override
	protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
		IFluidState ifluidstate = worldIn.getFluidState(pos);
		return ifluidstate.getFluid() == Fluids.WATER || state.getMaterial() == Material.ICE;
	}

	@Override
	public PlantType getPlantType(IBlockReader world, BlockPos pos){
		return PlantType.Water;
	}
}
