package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.api.ConfigUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.common.ToolAction;

import javax.annotation.Nullable;
import java.util.List;

public class Wrench extends Item{

	protected Wrench(){
		super(ESItems.baseItemProperties().stacksTo(1));
		String name = "wrench";
		ESItems.queueForRegister(name, this, ESConfig.addWrench.get() ? () -> new ItemStack[] {new ItemStack(this)} : null);
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction){
		//Wrench tooltype added as some other mods check tooltypes for wrenches
		return toolAction == ConfigUtil.WRENCH_ACTION;
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
