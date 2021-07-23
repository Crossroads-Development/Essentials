package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.entity.player.Player;
import net.minecraft.item.BlockPlaceContext ;
import net.minecraft.state.StateDefinition;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockHitResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.Level;
import net.minecraft.world.server.ServerLevel;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class AbstractCircuit extends AbstractTile{

	protected AbstractCircuit(String name){
		super(name);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext  context){
		return defaultBlockState().setValue(ESProperties.HORIZ_FACING, context.getHorizontalDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.HORIZ_FACING);//.add(EssentialsProperties.REDSTONE_BOOL);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		if(ESConfig.isWrench(playerIn.getItemInHand(hand))){
			if(!worldIn.isClientSide){
				worldIn.setBlockAndUpdate(pos, state.setValue(ESProperties.HORIZ_FACING, state.getValue(ESProperties.HORIZ_FACING).getClockWise()));
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}


	@Nullable
	@Override
	public BlockEntity newBlockEntity(IBlockReader worldIn){
		return new CircuitBlockEntity();
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof CircuitBlockEntity && !worldIn.isClientSide){
			CircuitBlockEntity cte = (CircuitBlockEntity) te;
			cte.builtConnections = false;
			cte.buildConnections();
		}else{
			worldIn.getBlockTicks().scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.VERY_HIGH);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		BlockEntity te = worldIn.getBlockEntity(pos);

		if(te instanceof CircuitBlockEntity){
			CircuitBlockEntity cte = (CircuitBlockEntity) te;
			if(blockIn == Blocks.REDSTONE_WIRE || blockIn instanceof RedstoneDiodeBlock){
				//Simple optimization- if the source of the block update is just a redstone signal changing, we don't need to force a full connection rebuild
				cte.handleInputChange(TickPriority.HIGH);
			}else{
				cte.builtConnections = false;
			}

			cte.buildConnections();
		}

		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Override
	public int getSignal(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side){
		if(side.getOpposite() == state.getValue(ESProperties.HORIZ_FACING)){
			BlockEntity te = blockAccess.getBlockEntity(pos);
			if(te instanceof CircuitBlockEntity){
				return RedstoneUtil.clampToVanilla(((CircuitBlockEntity) te).getOutput());
			}
		}
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side){
		return side != null && (side.getOpposite() == state.getValue(ESProperties.HORIZ_FACING) || useInput(CircuitBlockEntity.Orient.getOrient(side.getOpposite(), state.getValue(ESProperties.HORIZ_FACING))));
	}

	@Override
	public boolean isSignalSource(BlockState state){
		return true;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof CircuitBlockEntity){
			((CircuitBlockEntity) te).recalculateOutput();
		}
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		Direction facing = state.getValue(ESProperties.HORIZ_FACING);
		return side == facing || useInput(CircuitBlockEntity.Orient.getOrient(side, facing));
	}

	@Override
	public boolean usesQuartz(){
		return true;
	}

	/**
	 * Whether this device accepts a redstone input from a direction (relative to front)
	 * @param or The input orientation
	 * @return Whether this device accepts an input on that side
	 */
	public abstract boolean useInput(CircuitBlockEntity.Orient or);

	/**
	 * Calculates the output strength
	 * @param in0 CW input
	 * @param in1 Back input
	 * @param in2 CCW input
	 * @param te BlockEntity
	 * @return The output strength
	 */
	public abstract float getOutput(float in0, float in1, float in2, CircuitBlockEntity te);
}
