package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.TEBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class HopperFilter extends TEBlock{

	protected HopperFilter(){
		super(ESBlocks.getRockProperty());
		String name = "hopper_filter";
		ESBlocks.toRegister.put(name, this);
		ESBlocks.blockAddQue(name, this);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new HopperFilterTileEntity(pos, state);
	}

//	@Override
//	public BlockRenderLayer getRenderLayer(){
//		return BlockRenderLayer.CUTOUT;
//	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.hopper_filter.desc"));
		tooltip.add(Component.translatable("tt.essentials.hopper_filter.move"));
		tooltip.add(Component.translatable("tt.essentials.hopper_filter.shulker"));
	}

	private static final VoxelShape[] BB = new VoxelShape[3];

	static{
		BB[0] = Shapes.joinUnoptimized(box(0, 0, 0, 4, 16, 16), Shapes.joinUnoptimized(box(12, 0, 0, 16, 16, 16), box(4, 4, 4, 12, 12, 12), BooleanOp.OR), BooleanOp.OR);//X axis
		BB[1] = Shapes.joinUnoptimized(box(0, 0, 0, 16, 4, 16), Shapes.joinUnoptimized(box(0, 12, 0, 16, 16, 16), box(4, 4, 4, 12, 12, 12), BooleanOp.OR), BooleanOp.OR);//Y axis
		BB[2] = Shapes.joinUnoptimized(box(0, 0, 0, 16, 16, 4), Shapes.joinUnoptimized(box(0, 0, 12, 16, 16, 16), box(4, 4, 4, 12, 12, 12), BooleanOp.OR), BooleanOp.OR);//Z axis
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return BB[state.getValue(ESProperties.AXIS).ordinal()];
	}

	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.AXIS);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		if(state.getBlock() != newState.getBlock()){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof HopperFilterTileEntity){
				Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), ((HopperFilterTileEntity) te).getFilter());
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		if(ConfigUtil.isWrench(playerIn.getItemInHand(hand))){
			if(!worldIn.isClientSide){
				worldIn.setBlockAndUpdate(pos, state.cycle(ESProperties.AXIS));//MCP note: cycle
			}
			return InteractionResult.SUCCESS;
		}else{
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof HopperFilterTileEntity){
				if(!worldIn.isClientSide){
					HopperFilterTileEntity fte = (HopperFilterTileEntity) te;
					ItemStack held = playerIn.getItemInHand(hand);
					if(fte.getFilter().isEmpty() && !held.isEmpty()){
						fte.setFilter(held.split(1));
						playerIn.setItemInHand(hand, held);
					}else if(!fte.getFilter().isEmpty() && held.isEmpty()){
						playerIn.setItemInHand(hand, fte.getFilter());
						fte.setFilter(ItemStack.EMPTY);
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.FAIL;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(ESProperties.AXIS, context.getClickedFace().getAxis());
	}
}
