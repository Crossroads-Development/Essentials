package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.tileentities.ItemChutePortTileEntity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemChutePort extends BlockContainer{

	public ItemChutePort(){
		super(Material.IRON);
		String name = "item_chute_port";
		setUnlocalizedName(name);
		setRegistryName(name);
		setHardness(2);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setSoundType(SoundType.METAL);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new ItemChutePortTileEntity();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		EnumFacing enumfacing = (placer == null) ? EnumFacing.NORTH : placer.getHorizontalFacing().getOpposite();
		return getDefaultState().withProperty(EssentialsProperties.FACING, enumfacing);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState blockstate){
		ItemChutePortTileEntity te = (ItemChutePortTileEntity) world.getTileEntity(pos);
		te.dropItems();
		super.breakBlock(world, pos, blockstate);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand), worldIn.isRemote)){
			if(!worldIn.isRemote){
				worldIn.setBlockState(pos, state.withProperty(EssentialsProperties.FACING, state.getValue(EssentialsProperties.FACING).rotateY()));
			}
			return true;
		}
		return false;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(EssentialsProperties.FACING, rot.rotate(state.getValue(EssentialsProperties.FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
		return state.withRotation(mirrorIn.toRotation(state.getValue(EssentialsProperties.FACING)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced){
		if(Essentials.hasCrossroads && EssentialsConfig.getConfigBool(EssentialsConfig.itemChuteRotary, true)){
			tooltip.add("I: 2");
			tooltip.add("Consumes: 0.5J/operation");
		}
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.FACING, EssentialsProperties.CR_VERSION);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos){
		return state.withProperty(EssentialsProperties.CR_VERSION, Essentials.hasCrossroads && EssentialsConfig.getConfigBool(EssentialsConfig.itemChuteRotary, true));
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(EssentialsProperties.FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.FACING).getIndex();
	}
}
