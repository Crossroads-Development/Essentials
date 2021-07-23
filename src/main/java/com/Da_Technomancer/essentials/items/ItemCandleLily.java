package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WaterLilyBlockItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCandleLily extends WaterLilyBlockItem{

	public ItemCandleLily(){
		super(ESBlocks.candleLilyPad, ESBlocks.itemBlockProp);
		String name = "candle_lilypad";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.candle_lilypad.desc"));
	}
}
