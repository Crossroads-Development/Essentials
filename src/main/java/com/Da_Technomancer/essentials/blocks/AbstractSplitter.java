package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractSplitter extends ContainerBlock{

	protected AbstractSplitter(String name, Properties prop){
		super(prop);
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	protected abstract boolean isBasic();

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof AbstractSplitterTE){
			((AbstractSplitterTE) te).refreshCache();
		}
	}

	protected abstract ITextComponent getModeComponent(AbstractSplitterTE te, int newMode);

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult trace){
		if(ESConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				if(isBasic() && playerIn.isSneaking()){
					TileEntity te = worldIn.getTileEntity(pos);
					if(te instanceof AbstractSplitterTE){
						int mode = ((AbstractSplitterTE) te).increaseMode();
						playerIn.sendMessage(getModeComponent((AbstractSplitterTE) te, mode), playerIn.getUniqueID());
					}
				}else{
					worldIn.setBlockState(pos, state.func_235896_a_(ESProperties.FACING));//MCP note: cycle
				}
			}
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(ESProperties.FACING, (context.getPlayer() == null) ? Direction.NORTH : context.getNearestLookingDirection());
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}
