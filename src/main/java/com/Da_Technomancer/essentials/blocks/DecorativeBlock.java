package com.Da_Technomancer.essentials.blocks;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class DecorativeBlock extends Block{

	public DecorativeBlock(String name, Properties properties){
		super(properties);
		ESBlocks.toRegister.put(name, this);
		ESBlocks.blockAddQue(name, this);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.decoration"));
	}
}
