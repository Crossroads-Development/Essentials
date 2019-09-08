package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.tileentities.BasicItemSplitterTileEntity;
import com.Da_Technomancer.essentials.tileentities.ItemSplitterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
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
		super(Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3));
		String name = "item_splitter";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				BlockState endState = state.cycle(EssentialsProperties.FACING);
				worldIn.setBlockState(pos, endState);
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof BasicItemSplitterTileEntity){
					((BasicItemSplitterTileEntity) te).rotate();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new ItemSplitterTileEntity();
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		int i = Math.max(worldIn.getRedstonePower(pos.down(), Direction.DOWN), Math.max(worldIn.getRedstonePower(pos.up(), Direction.UP), Math.max(worldIn.getRedstonePower(pos.east(), Direction.EAST), Math.max(worldIn.getRedstonePower(pos.west(), Direction.WEST), Math.max(worldIn.getRedstonePower(pos.north(), Direction.NORTH), worldIn.getRedstonePower(pos.south(), Direction.SOUTH))))));
		i = Math.min(i, 15);
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof ItemSplitterTileEntity && ((ItemSplitterTileEntity) te).redstone != i){
			((ItemSplitterTileEntity) te).redstone = i;
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
