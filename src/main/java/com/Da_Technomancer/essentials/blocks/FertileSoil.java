package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FertileSoil extends Block{

	private final IBlockState plant;

	protected FertileSoil(String plantName, IBlockState plant){
		super(Block.Properties.create(plant.getBlock() == Blocks.NETHER_WART ? Material.SAND : Material.GROUND).hardnessAndResistance(0.5F).sound(SoundType.GROUND).tickRandomly());
		this.plant = plant;
		String name = "fertile_soil_" + plantName;
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public boolean isToolEffective(IBlockState state, ToolType tool){
		return tool == ToolType.SHOVEL;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TextComponentString("Slowly creates the seed plant/sapling on top of it, for free"));
		tooltip.add(new TextComponentString("Doesn't need a water source, and can't be trampled"));
		if(plant.getBlock() == Blocks.NETHER_WART){
			tooltip.add(new TextComponentString("Made with farmer souls"));
		}
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockReader world, BlockPos pos, EnumFacing direction, IPlantable plantable){
		return (plant.getBlock() == Blocks.NETHER_WART) == (plantable.getPlantType(world, pos.offset(direction)) == EnumPlantType.Nether);
	}

	@Override
	public boolean isFertile(IBlockState state, IBlockReader world, BlockPos pos){
		return true;
	}

	@Override
	public void tick(IBlockState state, World worldIn, BlockPos pos, Random random){
		if(worldIn.isRemote){
			return;
		}

		if(EssentialsConfig.fertileSoilRate.get() < 100D * Math.random()){
			return;
		}

		BlockPos upPos = pos.offset(EnumFacing.UP);

		if(worldIn.isAirBlock(upPos)){
			worldIn.setBlockState(upPos, plant);
		}
	}
}
