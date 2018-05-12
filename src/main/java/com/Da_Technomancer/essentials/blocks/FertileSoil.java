package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class FertileSoil extends Block{

	protected FertileSoil(){
		super(Material.GROUND);
		String name = "fertile_soil";
		setUnlocalizedName(name);
		setRegistryName(name);
		setHardness(.5F);
		setSoundType(SoundType.GROUND);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setTickRandomly(true);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQueRange(this, 9, new ItemMultiTexture(this, this, ((ItemMultiTexture.Mapper) (ItemStack stack) -> (stack.getMetadata() == 0 ? "wheat" : stack.getMetadata() == 1 ? "potato" : stack.getMetadata() == 2 ? "carrot" : stack.getMetadata() == 3 ? "beet" : stack.getMetadata() == 4 ? "oak" : stack.getMetadata() == 5 ? "birch" : stack.getMetadata() == 6 ? "spruce" : stack.getMetadata() == 7 ? "jungle" : stack.getMetadata() == 8 ? "acacia" : "dark"))));
	}

	@Override
	public boolean isToolEffective(String type, IBlockState state){
		return "shovel".equals(type);
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable){
		return true;
	}

	@Override
	public boolean isFertile(World world, BlockPos pos){
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
		if(state.getValue(EssentialsProperties.PLANT) >= 4){
			updateTick(worldIn, pos, state, RANDOM);
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand){
		if(worldIn.isRemote){
			return;
		}

		if(worldIn.isAirBlock(pos.offset(EnumFacing.UP))){
			switch(state.getValue(EssentialsProperties.PLANT)){
				case 0:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.WHEAT.getDefaultState().withProperty(BlockCrops.AGE, 0));
					break;
				case 1:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.POTATOES.getDefaultState().withProperty(BlockCrops.AGE, 0));
					break;
				case 2:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.CARROTS.getDefaultState().withProperty(BlockCrops.AGE, 0));
					break;
				case 3:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.BEETROOTS.getDefaultState().withProperty(BlockBeetroot.BEETROOT_AGE, 0));
					break;
				case 4:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK));
					break;
				case 5:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.BIRCH));
					break;
				case 6:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.SPRUCE));
					break;
				case 7:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.JUNGLE));
					break;
				case 8:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.ACACIA));
					break;
				case 9:
					worldIn.setBlockState(pos.offset(EnumFacing.UP), Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.DARK_OAK));
			}
		}
	}

	@Override
	public int damageDropped(IBlockState state){
		return state.getValue(EssentialsProperties.PLANT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list){
		Item item = Item.getItemFromBlock(this);
		for(int i = 0; i < 10; i++){
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.PLANT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(EssentialsProperties.PLANT, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.PLANT);
	}
}
