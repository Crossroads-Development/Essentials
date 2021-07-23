package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class AbstractCircuit extends AbstractTile{

	protected AbstractCircuit(String name){
		super(name);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return defaultBlockState().setValue(ESProperties.HORIZ_FACING, context.getHorizontalDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.HORIZ_FACING);//.add(EssentialsProperties.REDSTONE_BOOL);
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(ESConfig.isWrench(playerIn.getItemInHand(hand))){
			if(!worldIn.isClientSide){
				worldIn.setBlockAndUpdate(pos, state.setValue(ESProperties.HORIZ_FACING, state.getValue(ESProperties.HORIZ_FACING).getClockWise()));
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}


	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn){
		return new CircuitTileEntity();
	}

	@Override
	public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
		TileEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof CircuitTileEntity && !worldIn.isClientSide){
			CircuitTileEntity cte = (CircuitTileEntity) te;
			cte.builtConnections = false;
			cte.buildConnections();
		}else{
			worldIn.getBlockTicks().scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.VERY_HIGH);
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		TileEntity te = worldIn.getBlockEntity(pos);

		if(te instanceof CircuitTileEntity){
			CircuitTileEntity cte = (CircuitTileEntity) te;
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
			TileEntity te = blockAccess.getBlockEntity(pos);
			if(te instanceof CircuitTileEntity){
				return RedstoneUtil.clampToVanilla(((CircuitTileEntity) te).getOutput());
			}
		}
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side){
		return side != null && (side.getOpposite() == state.getValue(ESProperties.HORIZ_FACING) || useInput(CircuitTileEntity.Orient.getOrient(side.getOpposite(), state.getValue(ESProperties.HORIZ_FACING))));
	}

	@Override
	public boolean isSignalSource(BlockState state){
		return true;
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random){
		TileEntity te = worldIn.getBlockEntity(pos);
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
