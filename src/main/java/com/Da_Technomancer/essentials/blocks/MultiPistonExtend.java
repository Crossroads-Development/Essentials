package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MultiPistonExtend extends Block{

	private final boolean sticky;
	
	protected MultiPistonExtend(boolean sticky){
		super(Material.PISTON);
		this.sticky = sticky;
		String name = "multi_piston_extend" + (sticky ? "_sticky" : "");
		setUnlocalizedName(name);
		setRegistryName(name);
		setHardness(0.5F);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setDefaultState(getDefaultState().withProperty(EssentialsProperties.FACING, EnumFacing.NORTH).withProperty(EssentialsProperties.HEAD, false));
		EssentialsBlocks.toRegister.add(this);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if(world.isRemote){
			return;
		}

		if(world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING))).getBlock() == this && world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING))).getValue(EssentialsProperties.FACING) == state.getValue(EssentialsProperties.FACING)){
			world.setBlockState(pos.offset(state.getValue(EssentialsProperties.FACING)), Blocks.AIR.getDefaultState());
		}

		if(world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getBlock() == this && world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getValue(EssentialsProperties.FACING) == state.getValue(EssentialsProperties.FACING)){
			world.setBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite()), Blocks.AIR.getDefaultState());
		}else if(world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getBlock() == (sticky ? EssentialsBlocks.multiPistonSticky : EssentialsBlocks.multiPiston)){
			((MultiPistonBase) world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getBlock()).safeBreak(world, pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite()));
		}
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.FACING, EssentialsProperties.HEAD);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(EssentialsProperties.HEAD, (meta & 8) == 8).withProperty(EssentialsProperties.FACING, EnumFacing.getFront(meta & 7));
	}
	

	private static final AxisAlignedBB XBOX = new AxisAlignedBB(0, .375D, .375D, 1, .625D, .625D);
	private static final AxisAlignedBB YBOX = new AxisAlignedBB(.375D, 0, .375D, .625D, 1, .625D);
	private static final AxisAlignedBB ZBOX = new AxisAlignedBB(.375D, .375D, 0, .625D, .625D, 1);
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		if(state.getValue(EssentialsProperties.HEAD)){
			return FULL_BLOCK_AABB;
		}
		switch(state.getValue(EssentialsProperties.FACING).getAxis()){
			case X:
				return XBOX;
			case Y:
				return YBOX;
			case Z:
				return ZBOX;
			default:
				return null;
			
		}
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.FACING).getIndex() + (state.getValue(EssentialsProperties.HEAD) ? 8 : 0);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}
	
	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state){
		return EnumPushReaction.BLOCK;
	}
}