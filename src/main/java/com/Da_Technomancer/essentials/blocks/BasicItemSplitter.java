package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new BasicItemSplitterTileEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new StringTextComponent("Splits incoming items between the two outputs"));
		tooltip.add(new StringTextComponent("Configure splitting ratio by shift-right-clicking with a Wrench"));
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult trace){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				if(playerIn.isSneaking()){
					TileEntity te = worldIn.getTileEntity(pos);
					if(te instanceof BasicItemSplitterTileEntity){
						int mode = ((BasicItemSplitterTileEntity) te).increaseMode();
						playerIn.sendMessage(new StringTextComponent("Sending " + BasicItemSplitterTileEntity.MODES[mode] + "/4 of items downwards"));
					}
				}else{
					BlockState endState = state.cycle(EssentialsProperties.FACING);
					worldIn.setBlockState(pos, endState);
					TileEntity te = worldIn.getTileEntity(pos);
					if(te instanceof BasicItemSplitterTileEntity){
						((BasicItemSplitterTileEntity) te).facing = null;
					}
				}
			}
			return true;
		}

		return false;
	}


	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.FACING, (context.getPlayer() == null) ? Direction.NORTH : context.getNearestLookingDirection());
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(EssentialsProperties.FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}
