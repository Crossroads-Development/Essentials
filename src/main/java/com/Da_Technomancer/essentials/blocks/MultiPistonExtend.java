package com.Da_Technomancer.essentials.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

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
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving){
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
	public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack){
		Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), getCloneItemStack(state, null, worldIn, pos, player));
		super.playerDestroy(worldIn, player, pos, state, te, stack);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.AXIS, ESProperties.HEAD);
	}

	private static final VoxelShape[] ROD_BB = new VoxelShape[] {box(0.001D, 6, 6, 15.999D, 10, 10), box(6, 0.001D, 6, 10, 15.999D, 10), box(6, 6, 0.001D, 10, 10, 15.999D)};
	private static final VoxelShape[] HEAD_BB = new VoxelShape[] {box(0, 0, 0, 16, 5, 16), box(0, 11, 0, 16, 16, 16), box(0, 0, 0, 16, 16, 5), box(0, 0, 11, 16, 16, 16), box(0, 0, 0, 5, 16, 16), box(11D, 0, 0, 16, 16, 16)};

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		Direction.Axis axis = state.getValue(ESProperties.AXIS);
		Direction.AxisDirection dir = getDirFromHead(state.getValue(ESProperties.HEAD));
		if(dir == null){
			return ROD_BB[axis.ordinal()];
		}else{
			return Shapes.joinUnoptimized(ROD_BB[axis.ordinal()], HEAD_BB[Direction.get(dir, axis).get3DDataValue()], BooleanOp.OR);
		}
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state){
		return PushReaction.BLOCK;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player){
		return sticky ? new ItemStack(ESBlocks.multiPistonSticky, 1) : new ItemStack(ESBlocks.multiPiston, 1);
	}
}