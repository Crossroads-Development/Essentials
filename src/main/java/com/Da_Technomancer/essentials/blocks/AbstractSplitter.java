package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class AbstractSplitter extends BaseEntityBlock{

	protected AbstractSplitter(String name, Properties prop){
		super(prop);
		ESBlocks.toRegister.put(name, this);
		ESBlocks.blockAddQue(name, this);
	}

	protected abstract boolean isBasic();

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof AbstractSplitterTE){
			((AbstractSplitterTE) te).refreshCache();
		}
	}

	protected abstract Component getModeComponent(AbstractSplitterTE te, int newMode);

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult trace){
		if(ConfigUtil.isWrench(playerIn.getItemInHand(hand))){
			if(!worldIn.isClientSide){
				if(isBasic() && playerIn.isShiftKeyDown()){
					BlockEntity te = worldIn.getBlockEntity(pos);
					if(te instanceof AbstractSplitterTE){
						int mode = ((AbstractSplitterTE) te).increaseMode();
						playerIn.displayClientMessage(getModeComponent((AbstractSplitterTE) te, mode), true);
					}
				}else{
					worldIn.setBlockAndUpdate(pos, state.cycle(ESProperties.FACING));//MCP note: cycle
				}
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(ESProperties.FACING, (context.getPlayer() == null) ? Direction.NORTH : context.getNearestLookingDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.FACING);
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}
}
