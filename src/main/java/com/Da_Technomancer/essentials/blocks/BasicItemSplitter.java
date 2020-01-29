package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.tileentities.BasicItemSplitterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BasicItemSplitter extends ContainerBlock{

	protected BasicItemSplitter(Properties prop){
		super(prop);
	}

	public BasicItemSplitter(){
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(3));
		String name = "basic_item_splitter";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new BasicItemSplitterTileEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_basic"));
		tooltip.add(new TranslationTextComponent("tt.essentials.basic_item_splitter_formula"));
		tooltip.add(new TranslationTextComponent("tt.essentials.item_splitter_chute"));
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult trace){
		if(ESConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				if(playerIn.isCrouching()){
					TileEntity te = worldIn.getTileEntity(pos);
					if(te instanceof BasicItemSplitterTileEntity){
						int mode = ((BasicItemSplitterTileEntity) te).increaseMode();
						playerIn.sendMessage(new StringTextComponent(String.format("Sending %1$d/%2$d of items downwards", BasicItemSplitterTileEntity.MODES[mode], ((BasicItemSplitterTileEntity) te).getBase())));
					}
				}else{
					BlockState endState = state.cycle(ESProperties.FACING);
					worldIn.setBlockState(pos, endState);
					TileEntity te = worldIn.getTileEntity(pos);
					if(te instanceof BasicItemSplitterTileEntity){
						((BasicItemSplitterTileEntity) te).rotate();
					}
				}
			}
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}


	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof BasicItemSplitterTileEntity){
			((BasicItemSplitterTileEntity) te).refreshCache();
		}
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
