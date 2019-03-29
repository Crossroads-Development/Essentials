package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.tileentities.BasicItemSplitterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class BasicItemSplitter extends BlockContainer{

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
		tooltip.add(new TextComponentString("Splits incoming items between the two outputs"));
		tooltip.add(new TextComponentString("Configure splitting ratio with a Wrench"));
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof BasicItemSplitterTileEntity){
					int mode = ((BasicItemSplitterTileEntity) te).increaseMode();
					playerIn.sendMessage(new TextComponentString("Sending " + BasicItemSplitterTileEntity.MODES[mode] + "/4 of items downwards"));
				}
			}
			return true;
		}

		return false;
	}


	@Nullable
	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.FACING, (context.getPlayer() == null) ? EnumFacing.NORTH : context.getNearestLookingDirection());
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder){
		builder.add(EssentialsProperties.FACING);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
}
