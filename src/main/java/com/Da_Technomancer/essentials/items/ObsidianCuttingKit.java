package com.Da_Technomancer.essentials.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ObsidianCuttingKit extends Item{

	private static final TagKey<Block> OBSIDIAN_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "obsidians"));

	protected ObsidianCuttingKit(){
		super(ESItems.baseItemProperties());
		String name = "obsidian_cutting_kit";
		ESItems.queueForRegister(name, this);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.obsidian_kit.desc"));
	}

	@Override
	public InteractionResult useOn(UseOnContext context){
		if(context.getLevel().getBlockState(context.getClickedPos()).is(OBSIDIAN_TAG)){
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
