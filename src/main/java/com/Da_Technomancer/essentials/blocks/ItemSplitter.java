package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.tileentities.ItemSplitterBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSplitter extends BasicItemSplitter{

	public ItemSplitter(){
		super("item_splitter", Properties.of(Material.METAL).sound(SoundType.METAL).strength(3));
	}

	@Override
	protected boolean isBasic(){
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(IBlockReader world){
		return new ItemSplitterBlockEntity();
	}
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, flag);
		int i = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof ItemSplitterBlockEntity && ((ItemSplitterBlockEntity) te).redstone != i){
			((ItemSplitterBlockEntity) te).redstone = i;
			te.setChanged();
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_basic"));
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_formula"));
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_chute"));
	}
}
