package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.tileentities.ItemSplitterTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class ItemSplitter extends BasicItemSplitter{

	public ItemSplitter(){
		super("item_splitter", Properties.of(Material.METAL).sound(SoundType.METAL).strength(3));
	}

	@Override
	protected boolean isBasic(){
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new ItemSplitterTileEntity();
	}
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, flag);
		int i = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof ItemSplitterTileEntity && ((ItemSplitterTileEntity) te).redstone != i){
			((ItemSplitterTileEntity) te).redstone = i;
			te.setChanged();
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.item_splitter_basic"));
		tooltip.add(new TranslatableComponent("tt.essentials.item_splitter_formula"));
		tooltip.add(new TranslatableComponent("tt.essentials.item_splitter_chute"));
	}
}
