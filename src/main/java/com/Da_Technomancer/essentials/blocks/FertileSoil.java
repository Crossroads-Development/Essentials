package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FertileSoil extends Block{

	private final IBlockState plant;

	protected FertileSoil(String plantName, IBlockState plant){
		super(plant.getBlock() == Blocks.NETHER_WART ? Material.SAND : Material.GROUND);
		this.plant = plant;
		String name = "fertile_soil_" + plantName;
		setUnlocalizedName(name);
		setRegistryName(name);
		setHardness(.5F);
		setSoundType(SoundType.GROUND);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setTickRandomly(true);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public boolean isToolEffective(String type, IBlockState state){
		return "shovel".equals(type);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced){
		tooltip.add("Slowly creates the seed plant/sapling on top of it, for free");
		if(plant.getBlock() == Blocks.NETHER_WART){
			tooltip.add("Made with farmer souls");
		}
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable){
		return (plant.getBlock() == Blocks.NETHER_WART) == (plantable.getPlantType(world, pos.offset(direction)) == EnumPlantType.Nether);
	}

	@Override
	public boolean isFertile(World world, BlockPos pos){
		return true;
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand){
		if(worldIn.isRemote){
			return;
		}

		if(EssentialsConfig.getConfigDouble(EssentialsConfig.fertileSoilRate, false) < 100D * Math.random()){
			return;
		}

		BlockPos upPos = pos.offset(EnumFacing.UP);

		if(worldIn.isAirBlock(upPos)){
			worldIn.setBlockState(upPos, plant);
		}
	}
}
