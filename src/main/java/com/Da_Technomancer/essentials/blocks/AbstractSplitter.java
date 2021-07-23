package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class AbstractSplitter extends BaseEntityBlock{

	protected AbstractSplitter(String name, Properties prop){
		super(prop);
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
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
		if(ESConfig.isWrench(playerIn.getItemInHand(hand))){
			if(!worldIn.isClientSide){
				if(isBasic() && playerIn.isShiftKeyDown()){
					BlockEntity te = worldIn.getBlockEntity(pos);
					if(te instanceof AbstractSplitterTE){
						int mode = ((AbstractSplitterTE) te).increaseMode();
						playerIn.sendMessage(getModeComponent((AbstractSplitterTE) te, mode), playerIn.getUUID());
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
