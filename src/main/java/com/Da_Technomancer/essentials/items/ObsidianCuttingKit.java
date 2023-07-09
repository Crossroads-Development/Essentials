package com.Da_Technomancer.essentials.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.List;

public class ObsidianCuttingKit extends Item{

	protected ObsidianCuttingKit(){
		super(ESItems.baseItemProperties());
		String name = "obsidian_cutting_kit";
		ESItems.queueForRegister(name, this);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.obsidian_kit.desc"));
	}

	@Override
	public InteractionResult useOn(UseOnContext context){
		if(context.getLevel().getBlockState(context.getClickedPos()).getBlock() == Blocks.OBSIDIAN){
			if(!context.getLevel().isClientSide){
				context.getLevel().destroyBlock(context.getClickedPos(), true);
				if(context.getPlayer() == null || !context.getPlayer().isCreative()){
					context.getItemInHand().shrink(1);
				}
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}
}
