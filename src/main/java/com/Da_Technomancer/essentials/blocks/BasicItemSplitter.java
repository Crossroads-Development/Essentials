package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE;
import com.Da_Technomancer.essentials.tileentities.BasicItemSplitterTileEntity;
import net.minecraft.block.Block;
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

public class BasicItemSplitter extends AbstractSplitter{

	protected BasicItemSplitter(String name, Properties prop){
		super(name, prop);
	}

	public BasicItemSplitter(){
		super("basic_item_splitter", Block.Properties.create(Material.IRON).hardnessAndResistance(3));
	}

	@Override
	protected boolean isBasic(){
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new BasicItemSplitterTileEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_basic"));
		tooltip.add(new TranslationTextComponent("tt.essentials.basic_item_splitter_formula"));
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_chute"));
	}

	@Override
	protected ITextComponent getModeComponent(AbstractSplitterTE te, int newMode){
		return new TranslationTextComponent("tt.essentials.basic_item_splitter.mode", newMode, te.getDistribution().base);
	}
}
