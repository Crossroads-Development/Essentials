package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.EnumPlantType;

public class CandleLilyPad extends BlockLilyPad{

	protected CandleLilyPad(){
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0).sound(SoundType.PLANT));
		String name = "candle_lilypad";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
	}

	@Override
	public int getLightValue(IBlockState state, IWorldReader world, BlockPos pos){
		return 14;
	}

	@Override
	protected boolean isValidGround(IBlockState state, IBlockReader worldIn, BlockPos pos) {
		IFluidState ifluidstate = worldIn.getFluidState(pos);
		return ifluidstate.getFluid() == Fluids.WATER || state.getMaterial() == Material.ICE;
	}

	@Override
	public EnumPlantType getPlantType(IBlockReader world, BlockPos pos){
		return EnumPlantType.Water;
	}
}
