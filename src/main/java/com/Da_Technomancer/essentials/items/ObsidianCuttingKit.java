package com.Da_Technomancer.essentials.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
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
		tooltip.add(new TextComponentString("Insta-breaks obsidian on right click, but uses the tool"));
	}

	@Override
	public EnumActionResult onItemUse(ItemUseContext context){
		if(context.getWorld().getBlockState(context.getPos()).getBlock() == Blocks.OBSIDIAN){
			if(!context.getWorld().isRemote){
				context.getWorld().destroyBlock(context.getPos(), true);
				if(context.getPlayer() == null || !context.getPlayer().isCreative()){
					context.getItem().shrink(1);
				}
			}
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
