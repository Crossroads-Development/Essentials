package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.redstone.IReadable;
import com.Da_Technomancer.essentials.tileentities.SortingHopperTileEntity;
import net.minecraft.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class SortingHopper extends BaseEntityBlock implements IReadable{

	public static final DirectionProperty FACING = HopperBlock.FACING;
	public static final BooleanProperty ENABLED = HopperBlock.ENABLED;

	//Taken from vanilla hopper to ensure similarity
	private static final VoxelShape INPUT_SHAPE = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape MIDDLE_SHAPE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
	private static final VoxelShape INPUT_MIDDLE_SHAPE = Shapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
	private static final VoxelShape BASE = Shapes.join(INPUT_MIDDLE_SHAPE, Hopper.INSIDE, BooleanOp.ONLY_FIRST);
	private static final VoxelShape DOWN_SHAPE = Shapes.or(BASE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
	private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
	private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
	private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
	private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
	private static final VoxelShape DOWN_RAYTRACE_SHAPE = Hopper.INSIDE;
	private static final VoxelShape EAST_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
	private static final VoxelShape NORTH_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
	private static final VoxelShape SOUTH_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
	private static final VoxelShape WEST_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));


	protected SortingHopper(Properties prop){
		super(prop);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN).setValue(ENABLED, true));
	}

	protected SortingHopper(){
		this(Properties.of(Material.METAL).sound(SoundType.METAL).strength(2));
		String name = "sorting_hopper";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		switch(state.getValue(FACING)){
			case DOWN:
				return DOWN_SHAPE;
			case NORTH:
				return NORTH_SHAPE;
			case SOUTH:
				return SOUTH_SHAPE;
			case WEST:
				return WEST_SHAPE;
			case EAST:
				return EAST_SHAPE;
			default:
				return BASE;
		}
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos){
		switch(state.getValue(FACING)){
			case DOWN:
				return DOWN_RAYTRACE_SHAPE;
			case NORTH:
				return NORTH_RAYTRACE_SHAPE;
			case SOUTH:
				return SOUTH_RAYTRACE_SHAPE;
			case WEST:
				return WEST_RAYTRACE_SHAPE;
			case EAST:
				return EAST_RAYTRACE_SHAPE;
			default:
				return Hopper.INSIDE;
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		Direction enumfacing = context.getClickedFace().getOpposite();
		if(enumfacing == Direction.UP){
			enumfacing = Direction.DOWN;
		}

		return defaultBlockState().setValue(FACING, enumfacing).setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter world){
		return new SortingHopperTileEntity();
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		if(!worldIn.isClientSide){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(ESConfig.isWrench(playerIn.getItemInHand(hand))){
				worldIn.setBlockAndUpdate(pos, state.cycle(FACING));//MCP note: cycle
				if(te instanceof SortingHopperTileEntity){
					((SortingHopperTileEntity) te).resetCache();
				}
				return InteractionResult.SUCCESS;
			}

			if(te instanceof SortingHopperTileEntity){
				playerIn.openMenu((SortingHopperTileEntity) te);
//				playerIn.addStat(Stats.INSPECT_HOPPER);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		boolean block = !worldIn.hasNeighborSignal(pos);

		if(block != state.getValue(ENABLED)){
			worldIn.setBlock(pos, state.setValue(ENABLED, block), 4);
		}
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof SortingHopperTileEntity) {
				Containers.dropContents(worldIn, pos, (SortingHopperTileEntity) te);
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state){
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos){
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
	}

	@Override
	public float read(Level world, BlockPos pos, BlockState state){
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof Container){
			Container inv = (Container) te;
			float f = 0.0F;

			for(int i = 0; i < inv.getContainerSize(); i++){
				ItemStack stack = inv.getItem(i);
				if(!stack.isEmpty()){
					f += (float) stack.getCount() / (float) Math.min(64, stack.getMaxStackSize());
				}
			}

			f = f / (float) inv.getContainerSize();
			f *= 15F;
			return f;
		}
		return 0;
	}

//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public BlockRenderLayer getRenderLayer(){
//		return BlockRenderLayer.CUTOUT_MIPPED;
//	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot){
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(FACING, ENABLED);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.sorting_hopper.desc"));
		tooltip.add(new TranslatableComponent("tt.essentials.sorting_hopper.quip").setStyle(ESConfig.TT_QUIP));//MCP note: setStyle
	}
}
