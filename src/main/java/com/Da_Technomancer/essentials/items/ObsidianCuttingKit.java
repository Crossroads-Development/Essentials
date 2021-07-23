package com.Da_Technomancer.essentials.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ObsidianCuttingKit extends Item{

	protected ObsidianCuttingKit(){
		super(new Properties().tab(ESItems.TAB_ESSENTIALS));
		String name = "obsidian_cutting_kit";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tt.essentials.obsidian_kit.desc"));
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
