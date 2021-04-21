package com.Da_Technomancer.essentials.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class ObsidianCuttingKit extends Item{

	protected ObsidianCuttingKit(){
		super(new Properties().tab(ESItems.TAB_ESSENTIALS));
		String name = "obsidian_cutting_kit";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.obsidian_kit.desc"));
	}

	@Override
	public ActionResultType useOn(ItemUseContext context){
		if(context.getLevel().getBlockState(context.getClickedPos()).getBlock() == Blocks.OBSIDIAN){
			if(!context.getLevel().isClientSide){
				context.getLevel().destroyBlock(context.getClickedPos(), true);
				if(context.getPlayer() == null || !context.getPlayer().isCreative()){
					context.getItemInHand().shrink(1);
				}
			}
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}
}
