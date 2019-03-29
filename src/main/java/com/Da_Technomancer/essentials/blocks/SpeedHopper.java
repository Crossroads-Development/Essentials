package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.tileentities.SortingHopperTileEntity;
import com.Da_Technomancer.essentials.tileentities.SpeedHopperTileEntity;
import net.minecraft.block.SoundType;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class SpeedHopper extends SortingHopper{

	protected SpeedHopper(){
		super(Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(2));
		String name = "speed_hopper";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new SpeedHopperTileEntity();
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(!worldIn.isRemote){
			TileEntity te = worldIn.getTileEntity(pos);
			if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
				worldIn.setBlockState(pos, state.cycle(FACING));
				if(te instanceof SortingHopperTileEntity){
					((SortingHopperTileEntity) te).resetCache();
				}
				return true;
			}

			if(te instanceof SortingHopperTileEntity){
				playerIn.displayGUIChest((SortingHopperTileEntity) te);
				playerIn.addStat(StatList.INSPECT_HOPPER);
			}
		}
		return true;
	}

	@Override
	public void onPlayerDestroy(IWorld worldIn, BlockPos pos, IBlockState state){
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if(tileentity instanceof SortingHopperTileEntity){
			InventoryHelper.dropInventoryItems(worldIn.getWorld(), pos, (SortingHopperTileEntity) tileentity);
			worldIn.getWorld().updateComparatorOutputLevel(pos, this);
		}
		super.onPlayerDestroy(worldIn, pos, state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TextComponentString("Has the same sorting properties as a Sorting Hopper"));
		tooltip.add(new TextComponentString("Inserts or extracts entire stacks at a time"));
	}
}
