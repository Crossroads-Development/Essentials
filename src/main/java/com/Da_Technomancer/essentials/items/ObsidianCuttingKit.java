package com.Da_Technomancer.essentials.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ObsidianCuttingKit extends Item{

	protected ObsidianCuttingKit(){
		super(new Properties().group(EssentialsItems.TAB_ESSENTIALS));
		String name = "obsidian_cutting_kit";
		setRegistryName(name);
		EssentialsItems.toRegister.add(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new StringTextComponent("Insta-breaks obsidian on right click, but uses the tool"));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		if(context.getWorld().getBlockState(context.getPos()).getBlock() == Blocks.OBSIDIAN){
			if(!context.getWorld().isRemote){
				context.getWorld().destroyBlock(context.getPos(), true);
				if(context.getPlayer() == null || !context.getPlayer().isCreative()){
					context.getItem().shrink(1);
				}
			}
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}
}
