package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
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
import javax.sound.midi.SysexMessage;
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
		if(te instanceof CircuitTileEntity && !worldIn.isRemote){
			((CircuitTileEntity) te).buildConnections();
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean p_220069_6_){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof CircuitTileEntity){
			((CircuitTileEntity) te).buildConnections();
		}
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.HIGH);
	}

	@Override
	public int getWeakPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side){
		if(side.getOpposite() == state.get(EssentialsProperties.HORIZ_FACING)){
			TileEntity te = blockAccess.getTileEntity(pos);
			if(te instanceof CircuitTileEntity){
				return RedstoneUtil.clampToVanilla(((CircuitTileEntity) te).getOutput());
			}
		}
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side){
		return side != null && (side.getOpposite() == state.get(EssentialsProperties.HORIZ_FACING) || useInput(CircuitTileEntity.Orient.getOrient(side, state.get(EssentialsProperties.HORIZ_FACING))));
	}

	@Override
	public boolean canProvidePower(BlockState state){
		return true;
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
				playerIn.sendMessage(new StringTextComponent("OUT: " + ((CircuitTileEntity) te).getOutput() + ""));
			}
			return true;
		}
		//return false;
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

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof CircuitTileEntity){
			((CircuitTileEntity) te).recalculateOutput();
		}
	}
}
