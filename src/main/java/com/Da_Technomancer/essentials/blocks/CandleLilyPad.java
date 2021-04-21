package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.AbstractBlock;

public class CandleLilyPad extends LilyPadBlock{

	protected CandleLilyPad(){
		super(AbstractBlock.Properties.of(Material.PLANT).strength(0).sound(SoundType.GRASS).lightLevel(s -> 14));
		String name = "candle_lilypad";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
	}

	@Override
	public PlantType getPlantType(IBlockReader world, BlockPos pos){
		return PlantType.WATER;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.candle_lilypad.desc"));
	}
}
