package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.*;
import net.minecraft.block.material.Material;
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

public class FluidSplitter extends BasicFluidSplitter{

	public FluidSplitter(){
		super("fluid_splitter", Properties.create(Material.IRON).hardnessAndResistance(3));
	}

	@Override
	protected boolean isBasic(){
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new FluidSplitterTileEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_splitter_basic"));
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_splitter_formula"));
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_splitter_chute"));
	}
}
