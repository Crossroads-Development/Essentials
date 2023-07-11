package com.Da_Technomancer.essentials.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nullable;
import java.util.List;

public class CandleLilyPad extends WaterlilyBlock{

	protected CandleLilyPad(){
		super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).instabreak().sound(SoundType.LILY_PAD).lightLevel(s -> 14).noOcclusion());
		String name = "candle_lilypad";
		ESBlocks.queueForRegister(name, this, false);
	}

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos){
		return PlantType.WATER;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.candle_lilypad.desc"));
	}
}
