package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.tileentities.FluidShifterTileEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class FluidShifter extends AbstractShifter{

	protected FluidShifter(){
		super("fluid_shifter");
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter world){
		return new FluidShifterTileEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.fluid_shifter_info"));
		tooltip.add(new TranslatableComponent("tt.essentials.fluid_shifter_range", ESConfig.itemChuteRange.get()));
	}
}
