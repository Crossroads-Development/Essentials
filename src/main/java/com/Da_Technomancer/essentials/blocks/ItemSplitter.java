package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.tileentities.ItemSplitterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
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

public class ItemSplitter extends BasicItemSplitter{

	public ItemSplitter(){
		super("item_splitter", Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3));
	}

	@Override
	protected boolean isBasic(){
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new ItemSplitterTileEntity();
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		int i = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof ItemSplitterTileEntity && ((ItemSplitterTileEntity) te).redstone != i){
			((ItemSplitterTileEntity) te).redstone = i;
			te.markDirty();
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_basic"));
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_formula"));
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_chute"));
	}
}
