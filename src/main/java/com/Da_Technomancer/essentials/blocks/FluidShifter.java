package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.tileentities.FluidShifterTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class FluidShifter extends AbstractShifter{

	protected FluidShifter(){
		super("fluid_shifter");
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader world){
		return new FluidShifterTileEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_shifter_info"));
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_shifter_range", ESConfig.itemChuteRange.get()));
	}
}
