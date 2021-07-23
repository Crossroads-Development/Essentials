package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock.Properties;

public class MultiPistonExtend extends Block{

	private final boolean sticky;

	protected MultiPistonExtend(boolean sticky){
		super(Properties.of(Material.PISTON).strength(0.5F));
		this.sticky = sticky;
		String name = "multi_piston_extend" + (sticky ? "_sticky" : "");
		setRegistryName(name);
		registerDefaultState(defaultBlockState().setValue(ESProperties.AXIS, Direction.Axis.Y).setValue(ESProperties.HEAD, 0));
		ESBlocks.toRegister.add(this);
	}

	@Nullable
	protected static Direction.AxisDirection getDirFromHead(int head){
		if(head == 0){
			return null;
		}
		return Direction.AxisDirection.values()[head - 1];
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving){
		if(!MultiPistonBase.changingWorld){
			Direction.Axis axis = state.getValue(ESProperties.AXIS);
			Direction.AxisDirection dir = getDirFromHead(state.getValue(ESProperties.HEAD));

			Block piston = sticky ? ESBlocks.multiPistonSticky : ESBlocks.multiPiston;

			for(Direction.AxisDirection actDir : Direction.AxisDirection.values()){
				//Don't try to interact in the side with the piston head
				if(actDir != dir){
					BlockPos adjPos = pos.relative(Direction.get(actDir, axis));
					BlockState adjState = world.getBlockState(adjPos);
					//Even though under normal usage this if statement should be a guaranteed true, we check anyway in case we ended up in a glitched state, either through a bug or a player using the setblock command
					if((adjState.getBlock() == piston && adjState.getValue(ESProperties.FACING).getAxis() == axis) || (adjState.getBlock() == this && adjState.getValue(ESProperties.AXIS) == axis)){
						world.destroyBlock(adjPos, false);
					}
				}
			}
		}

		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public void playerDestroy(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack){
		InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), getPickBlock(state, null, worldIn, pos, player));
		super.playerDestroy(worldIn, player, pos, state, te, stack);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.AXIS, ESProperties.HEAD);
	}

	private static final VoxelShape[] ROD_BB = new VoxelShape[] {box(0.001D, 6, 6, 15.999D, 10, 10), box(6, 0.001D, 6, 10, 15.999D, 10), box(6, 6, 0.001D, 10, 10, 15.999D)};
	private static final VoxelShape[] HEAD_BB = new VoxelShape[] {box(0, 0, 0, 16, 5, 16), box(0, 11, 0, 16, 16, 16), box(0, 0, 0, 16, 16, 5), box(0, 0, 11, 16, 16, 16), box(0, 0, 0, 5, 16, 16), box(11D, 0, 0, 16, 16, 16)};

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		Direction.Axis axis = state.getValue(ESProperties.AXIS);
		Direction.AxisDirection dir = getDirFromHead(state.getValue(ESProperties.HEAD));
		if(dir == null){
			return ROD_BB[axis.ordinal()];
		}else{
			return VoxelShapes.joinUnoptimized(ROD_BB[axis.ordinal()], HEAD_BB[Direction.get(dir, axis).get3DDataValue()], IBooleanFunction.OR);
		}
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state){
		return PushReaction.BLOCK;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player){
		return sticky ? new ItemStack(ESBlocks.multiPistonSticky, 1) : new ItemStack(ESBlocks.multiPiston, 1);
	}
}