package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.BiFunction;

public abstract class AbstractCircuit extends AbstractTile{

	/**
	 * Some subclasses define their behavior based on these functions
	 * New functions can be added freely
	 * Must match on the server and client (not automatically synced)
	 */
	public static final HashMap<String, BiFunction<Float, Float, Float>> MATH_FUNCTION_MAPS = new HashMap<>(32);

	static{
		MATH_FUNCTION_MAPS.put("interface", (a, x) -> a);
		MATH_FUNCTION_MAPS.put("and", (a0, a1) -> a0 > 0 && a1 > 0 ? 1F : 0F);
		MATH_FUNCTION_MAPS.put("not", (a, x) -> a > 0 ? 0F : 1F);
		MATH_FUNCTION_MAPS.put("or", (a0, a1) -> a0 > 0 || a1 > 0 ? 1F : 0F);
		MATH_FUNCTION_MAPS.put("xor", (a0, a1) -> a0 > 0 ^ a1 > 0 ? 1F : 0F);
		MATH_FUNCTION_MAPS.put("max", Math::max);
		MATH_FUNCTION_MAPS.put("min", Math::min);
		MATH_FUNCTION_MAPS.put("sum", Float::sum);
		MATH_FUNCTION_MAPS.put("dif", (a, b) -> b - a);
		MATH_FUNCTION_MAPS.put("prod", (a0, a1) -> a0 * a1);
		MATH_FUNCTION_MAPS.put("quot", (a, b) -> b / a);
		MATH_FUNCTION_MAPS.put("pow", (a, b) -> (float) Math.pow(b, a));
		MATH_FUNCTION_MAPS.put("inv", (a, x) -> 1F / a);
		MATH_FUNCTION_MAPS.put("sin", (a, x) -> (float) Math.sin(a));
		MATH_FUNCTION_MAPS.put("cos", (a, x) -> (float) Math.cos(a));
		MATH_FUNCTION_MAPS.put("tan", (a, x) -> (float) Math.tan(a));
		MATH_FUNCTION_MAPS.put("asin", (a, x) -> (float) Math.asin(a));
		MATH_FUNCTION_MAPS.put("acos", (a, x) -> (float) Math.acos(a));
		MATH_FUNCTION_MAPS.put("atan", (a, x) -> (float) Math.atan(a));
		MATH_FUNCTION_MAPS.put("equals", (a0, a1) -> a0.equals(a1) || Math.abs(a0 - a1) / Math.max(a0, a1) <= 0.001F ? 1F : 0);//Checks if the smaller input is within 0.1% of the larger
		MATH_FUNCTION_MAPS.put("less", (a, b) -> b < a ? 1F : 0);
		MATH_FUNCTION_MAPS.put("more", (a, b) -> b > a ? 1F : 0);
		MATH_FUNCTION_MAPS.put("round", (a, x) -> (float) Math.round(a));
		MATH_FUNCTION_MAPS.put("floor", (a, x) -> (float) Math.floor(a));
		MATH_FUNCTION_MAPS.put("ceil", (a, x) -> (float) Math.ceil(a));
		MATH_FUNCTION_MAPS.put("log", (a, x) -> (float) Math.log10(a));
		MATH_FUNCTION_MAPS.put("modulo", (a, b) -> {
			a = Math.abs(a);
			return ((b % a) + a) % a;
		});//Does the clock modulus, not remainder modulus
		MATH_FUNCTION_MAPS.put("abs", (a, x) -> Math.abs(a));
		MATH_FUNCTION_MAPS.put("sign", (a, x) -> Math.signum(a));
	}

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
	public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		if(ConfigUtil.isWrench(stack)){
			if(!worldIn.isClientSide){
				worldIn.setBlockAndUpdate(pos, state.setValue(ESProperties.HORIZ_FACING, state.getValue(ESProperties.HORIZ_FACING).getClockWise()));
			}
			return ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}


	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new CircuitTileEntity(pos, state);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof CircuitTileEntity cte && !worldIn.isClientSide){
			cte.builtConnections = false;
			cte.buildConnections();
		}else{
			worldIn.scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.VERY_HIGH);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		BlockEntity te = worldIn.getBlockEntity(pos);

		if(te instanceof CircuitTileEntity cte){
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
	public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction side){
		return getSignal(state, world, pos, side);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean p_60519_){
		strongSignalBlockUpdates(world, pos, this, state.getValue(ESProperties.HORIZ_FACING));
		super.onRemove(state, world, pos, newState, p_60519_);
	}

	public static void strongSignalBlockUpdates(Level world, BlockPos pos, Block block, Direction facing){
		//As this outputs a strong signal, it needs to update neighbors in front and adjacent to the block in front
		Direction reverseDir = facing.getOpposite();
		BlockPos offsetPos = pos.relative(facing);
		world.neighborChanged(offsetPos, block, pos.relative(reverseDir));
		world.updateNeighborsAtExceptFromFacing(offsetPos, block, reverseDir);
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
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random){
		//This is called by Level::scheduleTick after a delay. Level::scheduleTick invoked by CircuitTileEntity::handleInputChange
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof CircuitTileEntity cte){
			cte.recalculateOutput();
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
	 * Actual circuit output only changes when CircuitTileEntity::setPower is called (which is handled automatically for changes in in0, in1, or in2, but not necessarily for changes in anything else)
	 * @param in0 CW input
	 * @param in1 Back input
	 * @param in2 CCW input
	 * @param te TileEntity
	 * @return The output strength
	 */
	public abstract float getOutput(float in0, float in1, float in2, CircuitTileEntity te);
}
