package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class AbstractCircuit extends ContainerBlock{

	protected static final Properties PROP = Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0, 0).sound(SoundType.WOOD);

	public AbstractCircuit(String name){
		super(PROP);
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
		setDefaultState(getDefaultState().with(EssentialsProperties.REDSTONE_BOOL, false));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.HORIZ_FACING, context.getPlacementHorizontalFacing());
	}

	protected static final VoxelShape BB = makeCuboidShape(0, 0, 0,16, 4, 16);

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return BB;
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(EssentialsProperties.HORIZ_FACING).add(EssentialsProperties.REDSTONE_BOOL);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn){
		return new CircuitTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof CircuitTileEntity){
			((CircuitTileEntity) te).addBlock();
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_){
		BlockPos relPos = fromPos.subtract(pos);
		Direction dir = Direction.getFacingFromVector(relPos.getX(), relPos.getY(), relPos.getZ());
		if(relPos.distanceSq(BlockPos.ZERO) != 0 && useInput(CircuitTileEntity.calcOrientIndex(dir, state.get(EssentialsProperties.HORIZ_FACING)))){
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.HIGH);
		}
	}

	@Override
	public int getWeakPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side){
		if(side.getOpposite() == state.get(EssentialsProperties.HORIZ_FACING)){
			TileEntity te = blockAccess.getTileEntity(pos);
			if(te instanceof CircuitTileEntity){
				return RedstoneUtil.clampToVanilla(((CircuitTileEntity) te).outHandler.getOutput());
			}
		}
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side){
		return side != null && (side.getOpposite() == state.get(EssentialsProperties.HORIZ_FACING) || useInput((3 + side.getOpposite().getHorizontalIndex() - state.get(EssentialsProperties.HORIZ_FACING).getHorizontalIndex()) & 3));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				worldIn.setBlockState(pos, state.cycle(EssentialsProperties.HORIZ_FACING));
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof CircuitTileEntity){
					((CircuitTileEntity) te).wipeCache();
				}
			}
			return true;
		}else{
			//TODO TEMP FOR TESTING
			TileEntity te = worldIn.getTileEntity(pos);
			if(!worldIn.isRemote && te instanceof CircuitTileEntity){
				playerIn.sendMessage(new StringTextComponent("OUT: " + ((CircuitTileEntity) te).outHandler.getOutput() + ""));
				playerIn.sendMessage(new StringTextComponent("IN_0: " + ((CircuitTileEntity) te).inputStr[0] + "; IN_1: " + ((CircuitTileEntity) te).inputStr[1] + "; IN_2: " + ((CircuitTileEntity) te).inputStr[2]));
			}
			return true;
		}
		//return false;
	}

	/**
	 * Whether this device accepts a redstone input from a direction (relative to front)
	 * @param index 0: Left, 1: Back, 2: Right
	 * @return Whether this device accepts an input on that side
	 */
	public abstract boolean useInput(int index);

	public abstract float getOutput(float in0, float in1, float in2, CircuitTileEntity te);

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof CircuitTileEntity){
			((CircuitTileEntity) te).recalculateOutput();
		}
	}
}
