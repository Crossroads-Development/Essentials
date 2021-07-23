package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.server.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FertileSoil extends Block{

	private final BlockState plant;
	private final SeedCategory category;

	protected FertileSoil(String plantName, BlockState plant, SeedCategory category){
		super(AbstractBlock.Properties.of(category == SeedCategory.HELL_CROP ? Material.SAND : Material.DIRT).strength(0.5F).sound(SoundType.GRAVEL).randomTicks());
		this.plant = plant;
		this.category = category;
		String name = "fertile_soil_" + plantName;
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_){
		return super.getDrops(p_220076_1_, p_220076_2_);
	}

	@Override
	public boolean isToolEffective(BlockState state, ToolType tool){
		return tool == ToolType.SHOVEL;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.fertile_soil.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.fertile_soil.benefits"));
		if(category == SeedCategory.HELL_CROP){
			tooltip.add(new TranslationTextComponent("tt.essentials.fertile_soil.quip").setStyle(ESConfig.TT_QUIP));//MCP note: setStyle
		}
	}

	@Override
	public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction direction, IPlantable plantable){
		//No longer needed as of MC1.16 due to large spruce trees using the dirt tag
//		//As it turns out, the same method determines whether a block is a valid soil for a plant type, and whether it should be turned into podzol by large spruce trees
//		//We do want to act as a soil, we don't want to become podzol
//		//So, in the special case of spruce trees, we read the stacktrace and see if this method is being called for podzol-ification, and if so, return false
//		if(plant.getBlock() == Blocks.SPRUCE_SAPLING){
//			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
//			String name;
//			//Because there's a lambda function in the stacktrace, different compilers disagree on the stacktrace below the lambda. In practice, placePodzolAt can be either at index 6 or 7, so we check both
//			if(stack.length > 7 && ((name = stack[6].getMethodName()).equals(ReflectionUtil.EsReflection.PODZOL_GEN.mcp) || name.equals(ReflectionUtil.EsReflection.PODZOL_GEN.obf) || (name = stack[7].getMethodName()).equals(ReflectionUtil.EsReflection.PODZOL_GEN.mcp) || name.equals(ReflectionUtil.EsReflection.PODZOL_GEN.obf))){
//				return false;
//			}
//		}

		return plant.getBlock() == plantable;
	}

	@Override
	public boolean isFertile(BlockState state, IBlockReader world, BlockPos pos){
		return true;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random){
		if(ESConfig.fertileSoilRate.get() < 100D * Math.random()){
			return;
		}

		BlockPos upPos = pos.relative(Direction.UP);
		//Check light levels are high enough if this is a crop
		if(worldIn.isEmptyBlock(upPos) && (category != SeedCategory.CROP || worldIn.getRawBrightness(upPos, 0) > 7)){
			worldIn.setBlockAndUpdate(upPos, plant);
		}
	}

	public enum SeedCategory{

		CROP(),
		TREE(),
		HELL_CROP(),
		BERRY(),
		MUSHROOM();

	}
}
