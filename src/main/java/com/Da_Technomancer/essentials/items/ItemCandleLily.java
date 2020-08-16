package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LilyPadItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCandleLily extends LilyPadItem{

	public ItemCandleLily(){
		super(ESBlocks.candleLilyPad, ESBlocks.itemBlockProp);
		String name = "candle_lilypad";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.candle_lilypad.desc"));
	}
}
