package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE;
import net.minecraft.block.Block;
import net.minecraft.block.RenderShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.BaseEntityBlock;
import net.minecraft.entity.player.Player;
import net.minecraft.item.BlockPlaceContext ;
import net.minecraft.state.StateDefinition;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockHitResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Level;

import javax.annotation.Nullable;

import net.minecraft.block.AbstractBlock.Properties;

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

	protected abstract ITextComponent getModeComponent(AbstractSplitterTE te, int newMode);

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
	public BlockState getStateForPlacement(BlockPlaceContext  context){
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
