package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.ReflectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FertileSoil extends Block{

	private final BlockState plant;

	protected FertileSoil(String plantName, BlockState plant){
		super(Block.Properties.create(plant.getBlock() == Blocks.NETHER_WART ? Material.SAND : Material.EARTH).hardnessAndResistance(0.5F).sound(SoundType.GROUND).tickRandomly());
		this.plant = plant;
		String name = "fertile_soil_" + plantName;
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool){
		return tool == ToolType.SHOVEL;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.fertile_soil.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.fertile_soil.benefits"));
		if(plant.getBlock() == Blocks.NETHER_WART){
			tooltip.add(new TranslationTextComponent("tt.essentials.fertile_soil.quip").func_230530_a_(ESConfig.TT_QUIP));
		}
	}

	@Override
	public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction direction, IPlantable plantable){
		//As it turns out, the same method determines whether a block is a valid soil for a plant type, and whether it should be turned into podzol by large spruce trees
		//We do want to act as a soil, we don't want to become podzol
		//So, in the special case of spruce trees, we read the stacktrace and see if this method is being called for podzol-ification, and if so, return false
		//TODO
		if(false && plant.getBlock() == Blocks.SPRUCE_SAPLING){
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			String name;
//			for(int i = 0; i < Math.min(stack.length, 8); i++)
//			Minecraft.getInstance().player.sendMessage(new StringTextComponent(i + ": " + stack[i].getMethodName()));//
			//Because there's a lambda function in the stacktrace, different compilers disagree on the stacktrace below the lambda. In practice, placePodzolAt can be either at index 6 or 7, so we check both
			if(stack.length > 7 && ((name = stack[6].getMethodName()).equals(ReflectionUtil.EsReflection.PODZOL_GEN.mcp) || name.equals(ReflectionUtil.EsReflection.PODZOL_GEN.obf) || (name = stack[7].getMethodName()).equals(ReflectionUtil.EsReflection.PODZOL_GEN.mcp) || name.equals(ReflectionUtil.EsReflection.PODZOL_GEN.obf))){
				return false;
			}
		}

		return plant.getBlock() == plantable;
	}

	@Override
	public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos){
		return true;
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random){
		if(ESConfig.fertileSoilRate.get() < 100D * Math.random()){
			return;
		}

		BlockPos upPos = pos.offset(Direction.UP);

		if(worldIn.isAirBlock(upPos)){
			worldIn.setBlockState(upPos, plant);
		}
	}
}
