package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.ESConfig;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class Wrench extends Item{

	protected Wrench(){
		//Wrench tooltype added as some other mods check tooltypes for wrenches
		super(new Item.Properties().stacksTo(1).tab(ESItems.TAB_ESSENTIALS).addToolType(ToolType.get("wrench"), 0));
		String name = "wrench";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	public Collection<ItemGroup> getCreativeTabs(){
		if(ESConfig.addWrench.get()){
			return super.getCreativeTabs();
		}
		return ImmutableList.of();
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player){
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.wrench"));
	}
}
