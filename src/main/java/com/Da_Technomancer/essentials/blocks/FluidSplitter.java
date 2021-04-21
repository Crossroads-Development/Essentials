package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.tileentities.FluidSplitterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.AbstractBlock.Properties;

public class FluidSplitter extends BasicFluidSplitter{

	public FluidSplitter(){
		super("fluid_splitter", Properties.of(Material.METAL).strength(3));
	}

	@Override
	protected boolean isBasic(){
		return false;
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader world){
		return new FluidSplitterTileEntity();
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		int i = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
		TileEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof FluidSplitterTileEntity && ((FluidSplitterTileEntity) te).redstone != i){
			((FluidSplitterTileEntity) te).redstone = i;
			te.setChanged();
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_splitter_basic"));
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_splitter_formula"));
		tooltip.add(new TranslationTextComponent("tt.essentials.fluid_splitter_chute"));
	}
}
