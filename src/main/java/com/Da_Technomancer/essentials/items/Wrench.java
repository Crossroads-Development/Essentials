package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class Wrench extends Item{

	protected Wrench(){
		//Wrench tooltype added as some other mods check tooltypes for wrenches
		super(new Item.Properties().stacksTo(1).tab(ESItems.TAB_ESSENTIALS));
		String name = "wrench";
		ESItems.toRegister.put(name, this);
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction){
		return toolAction == ConfigUtil.WRENCH_ACTION;
	}

	@Override
	public Collection<CreativeModeTab> getCreativeTabs(){
		if(ESConfig.addWrench.get()){
			return super.getCreativeTabs();
		}
		return ImmutableList.of();
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player){
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.wrench"));
	}
}
