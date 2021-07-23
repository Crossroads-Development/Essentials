package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class AbstractCircuit extends AbstractTile{

	protected AbstractCircuit(String name){
		super(name);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
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
	public BlockEntity newBlockEntity(BlockGetter worldIn){
		return new CircuitTileEntity();
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof CircuitTileEntity && !worldIn.isClientSide){
			CircuitTileEntity cte = (CircuitTileEntity) te;
			cte.builtConnections = false;
			cte.buildConnections();
		}else{
			worldIn.getBlockTicks().scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.VERY_HIGH);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		BlockEntity te = worldIn.getBlockEntity(pos);

		if(te instanceof CircuitTileEntity){
			CircuitTileEntity cte = (CircuitTileEntity) te;
			if(blockIn == Blocks.REDSTONE_WIRE || blockIn instanceof DiodeBlock){
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
	public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side){
		if(side.getOpposite() == state.getValue(ESProperties.HORIZ_FACING)){
			BlockEntity te = blockAccess.getBlockEntity(pos);
			if(te instanceof CircuitTileEntity){
				return RedstoneUtil.clampToVanilla(((CircuitTileEntity) te).getOutput());
			}
		}
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side){
		return side != null && (side.getOpposite() == state.getValue(ESProperties.HORIZ_FACING) || useInput(CircuitTileEntity.Orient.getOrient(side.getOpposite(), state.getValue(ESProperties.HORIZ_FACING))));
	}

	@Override
	public boolean isSignalSource(BlockState state){
		return true;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof CircuitTileEntity){
			((CircuitTileEntity) te).recalculateOutput();
		}
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		Direction facing = state.getValue(ESProperties.HORIZ_FACING);
		return side == facing || useInput(CircuitTileEntity.Orient.getOrient(side, facing));
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
	public abstract boolean useInput(CircuitTileEntity.Orient or);

	/**
	 * Calculates the output strength
	 * @param in0 CW input
	 * @param in1 Back input
	 * @param in2 CCW input
	 * @param te TileEntity
	 * @return The output strength
	 */
	public abstract float getOutput(float in0, float in1, float in2, CircuitTileEntity te);
}
