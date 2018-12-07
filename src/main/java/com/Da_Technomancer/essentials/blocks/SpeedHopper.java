package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.tileentities.SortingHopperTileEntity;
import com.Da_Technomancer.essentials.tileentities.SpeedHopperTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class SpeedHopper extends SortingHopper{

	protected SpeedHopper(){
		super(Material.IRON);
		String name = "speed_hopper";
		setTranslationKey(name);
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new SpeedHopperTileEntity();
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(!worldIn.isRemote){
			TileEntity te = worldIn.getTileEntity(pos);
			if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand), false)){
				worldIn.setBlockState(pos, state.cycleProperty(FACING));
				if(te instanceof SortingHopperTileEntity){
					((SortingHopperTileEntity) te).resetCache();
				}
				return true;
			}

			if(te instanceof SortingHopperTileEntity){
				playerIn.displayGUIChest((SortingHopperTileEntity) te);
				playerIn.addStat(StatList.HOPPER_INSPECTED);
			}
		}
		return true;
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof SortingHopperTileEntity){
			InventoryHelper.dropInventoryItems(worldIn, pos, (SortingHopperTileEntity) tileentity);
			worldIn.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced){
		tooltip.add("Inserts or extracts entire stacks at a time");
		tooltip.add("Exactly the same, aside from all the differences");
	}
}
