package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.gui.EssentialsGuiHandler;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.tileentities.FluidShifterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class FluidShifter extends BlockContainer{

	protected FluidShifter(){
		super(Material.IRON);
		String name = "fluid_shifter";
		setTranslationKey(name);
		setRegistryName(name);
		setHardness(2);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setSoundType(SoundType.METAL);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new FluidShifterTileEntity();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return getDefaultState().withProperty(EssentialsProperties.FACING, placer == null ? EnumFacing.UP : EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState blockstate){
		InventoryHelper.dropInventoryItems(world, pos, (FluidShifterTileEntity) world.getTileEntity(pos));
		super.breakBlock(world, pos, blockstate);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		boolean isWrench = EssentialsConfig.isWrench(playerIn.getHeldItem(hand), worldIn.isRemote);
		if(!worldIn.isRemote){
			if(isWrench){
				worldIn.setBlockState(pos, state.cycleProperty(EssentialsProperties.FACING));
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof FluidShifterTileEntity){
					((FluidShifterTileEntity) te).refreshCache();
				}
			}else{
				playerIn.openGui(Essentials.instance, EssentialsGuiHandler.FLUID_SHIFTER_GUI, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
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
		tooltip.add("Ejects contained fluid out the faced side into an attached inventory");
		tooltip.add("Can 'push' items through a line of up to " + EssentialsConfig.getConfigInt(EssentialsConfig.itemChuteRange, true) + " Transport Chutes");
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.FACING);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(EssentialsProperties.FACING, EnumFacing.byIndex(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.FACING).getIndex();
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof FluidShifterTileEntity){
			((FluidShifterTileEntity) te).refreshCache();
		}
	}
}
