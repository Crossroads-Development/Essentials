package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class MultiPistonExtend extends Block{

	private final boolean sticky;

	protected MultiPistonExtend(boolean sticky){
		super(Properties.create(Material.PISTON).hardnessAndResistance(0.5F));
		this.sticky = sticky;
		String name = "multi_piston_extend" + (sticky ? "_sticky" : "");
		setRegistryName(name);
		setDefaultState(getDefaultState().with(EssentialsProperties.AXIS, EnumFacing.Axis.Y).with(EssentialsProperties.HEAD, 0));
		EssentialsBlocks.toRegister.add(this);
	}

	@Nullable
	protected static EnumFacing.AxisDirection getDirFromHead(int head){
		if(head == 0){
			return null;
		}
		return EnumFacing.AxisDirection.values()[head - 1];
	}

	@Override
	public void dropBlockAsItemWithChance(IBlockState state, World worldIn, BlockPos pos, float chancePerItem, int fortune){
		if(!MultiPistonBase.changingWorld){
			Block piston = sticky ? EssentialsBlocks.multiPistonSticky : EssentialsBlocks.multiPiston;
			NonNullList<ItemStack> drops = NonNullList.create();
			piston.getDrops(state, drops, worldIn, pos, fortune);
			for(ItemStack s : drops){
				spawnAsEntity(worldIn, pos, s);
			}
		}
	}

	@Override
	public void onPlayerDestroy(IWorld world, BlockPos pos, IBlockState state){
		if(!MultiPistonBase.changingWorld){
			EnumFacing.Axis axis = state.get(EssentialsProperties.AXIS);
			EnumFacing.AxisDirection dir = getDirFromHead(state.get(EssentialsProperties.HEAD));

			Block piston = sticky ? EssentialsBlocks.multiPistonSticky : EssentialsBlocks.multiPiston;

			for(EnumFacing.AxisDirection actDir : EnumFacing.AxisDirection.values()){
				//Don't try to interact in the side with the piston head
				if(actDir != dir){
					BlockPos adjPos = pos.offset(EnumFacing.getFacingFromAxis(actDir, axis));
					IBlockState adjState = world.getBlockState(adjPos);
					//Even though under normal usage this if statement should be a guaranteed true, we check anyway in case we ended up in a glitched state, either through a bug or a player using the setblock command
					if((adjState.getBlock() == piston && adjState.get(EssentialsProperties.FACING).getAxis() == axis) || (adjState.getBlock() == this && adjState.get(EssentialsProperties.AXIS) == axis)){
						world.removeBlock(adjPos);
					}
				}
			}
		}
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder){
		builder.add(EssentialsProperties.AXIS, EssentialsProperties.HEAD);
	}

	private static final VoxelShape[] ROD_BB = new VoxelShape[] {makeCuboidShape(0.001D, .375D, .375D, 0.999D, .625D, .625D), makeCuboidShape(.375D, 0.001D, .375D, .625D, 0.999D, .625D), makeCuboidShape(.375D, .375D, 0.001D, .625D, .625D, 0.999D)};
	private static final VoxelShape[] HEAD_BB = new VoxelShape[] {makeCuboidShape(0, 0, 0, 1, 5D / 16D, 1), makeCuboidShape(0, 11D / 16D, 0, 1, 1, 1), makeCuboidShape(0, 0, 0, 1, 1, 5D / 16D), makeCuboidShape(0, 0, 11D / 16D, 1, 1, 1), makeCuboidShape(0, 0, 0, 5D / 16D, 1, 1), makeCuboidShape(11D / 16D, 0, 0, 1, 1, 1)};

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos){
		EnumFacing.Axis axis = state.get(EssentialsProperties.AXIS);
		EnumFacing.AxisDirection dir = getDirFromHead(state.get(EssentialsProperties.HEAD));
		if(dir == null){
			return ROD_BB[axis.ordinal()];
		}else{
			return VoxelShapes.combine(ROD_BB[axis.ordinal()], HEAD_BB[EnumFacing.getFacingFromAxis(dir, axis).getIndex()], IBooleanFunction.OR);
		}
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state){
		return EnumPushReaction.BLOCK;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		if(face.getAxis() == state.get(EssentialsProperties.AXIS)){
			EnumFacing.AxisDirection dir  = getDirFromHead(state.get(EssentialsProperties.HEAD));
			if(dir == null || face.getAxisDirection() != dir){
				return BlockFaceShape.MIDDLE_POLE_THIN;
			}
			return BlockFaceShape.SOLID;
		}
		return BlockFaceShape.UNDEFINED;
	}
}