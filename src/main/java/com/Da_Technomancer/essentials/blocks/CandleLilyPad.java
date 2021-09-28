package com.Da_Technomancer.essentials.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nullable;
import java.util.List;

public class CandleLilyPad extends WaterlilyBlock{

	protected CandleLilyPad(){
		super(BlockBehaviour.Properties.of(Material.PLANT).strength(0).sound(SoundType.GRASS).lightLevel(s -> 14));
		String name = "candle_lilypad";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
	}

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos){
		return PlantType.WATER;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tt.essentials.candle_lilypad.desc"));
	}
}
