package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class CandleLilyPad extends BlockLilyPad{

	protected CandleLilyPad(){
		String name = "candle_lilypad";
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setUnlocalizedName(name);
		setRegistryName(name);
		setSoundType(SoundType.PLANT);
		EssentialsBlocks.toRegister.add(this);
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos){
		return 14;
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos){
		IBlockState soil = worldIn.getBlockState(pos.down());
		return super.canPlaceBlockAt(worldIn, pos) && soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, (IPlantable) Blocks.WATERLILY);
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos){
		return EnumPlantType.Water;
	}

}
