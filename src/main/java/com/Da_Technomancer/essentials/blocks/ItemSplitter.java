package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.ItemSplitterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSplitter extends BlockContainer{

	public ItemSplitter(){
		super(Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(3));
		String name = "item_splitter";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new ItemSplitterTileEntity();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
		neighborChanged(null, world, pos, null, null);
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
		int i = Math.max(worldIn.getRedstonePower(pos.down(), EnumFacing.DOWN), Math.max(worldIn.getRedstonePower(pos.up(), EnumFacing.UP), Math.max(worldIn.getRedstonePower(pos.east(), EnumFacing.EAST), Math.max(worldIn.getRedstonePower(pos.west(), EnumFacing.WEST), Math.max(worldIn.getRedstonePower(pos.north(), EnumFacing.NORTH), worldIn.getRedstonePower(pos.south(), EnumFacing.SOUTH))))));
		i = Math.min(i, 15);
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof ItemSplitterTileEntity && ((ItemSplitterTileEntity) te).redstone != i){
			((ItemSplitterTileEntity) te).redstone = i;
		}
	}

	@Nullable
	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.FACING, context.getNearestLookingDirection());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TextComponentString("Splits incoming items between the two outputs"));
		tooltip.add(new TextComponentString("Splitting ratio controlled via redstone signal strength: (signal strength)/15"));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder){
		builder.add(EssentialsProperties.FACING);
	}
}
