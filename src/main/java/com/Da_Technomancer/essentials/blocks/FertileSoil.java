package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.api.ConfigUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.List;

public class FertileSoil extends Block{

	private final BlockState plant;
	private final SeedCategory category;

	protected FertileSoil(String plantName, BlockState plant, SeedCategory category){
		super(BlockBehaviour.Properties.of().mapColor(category == SeedCategory.HELL_CROP ? MapColor.SAND : MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL).randomTicks());
		this.plant = plant;
		this.category = category;
		String name = "fertile_soil_" + plantName;
		ESBlocks.queueForRegister(name, this);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.fertile_soil.desc"));
		tooltip.add(Component.translatable("tt.essentials.fertile_soil.benefits"));
		if(category == SeedCategory.HELL_CROP){
			tooltip.add(Component.translatable("tt.essentials.fertile_soil.quip").setStyle(ConfigUtil.TT_QUIP));//MCP note: setStyle
		}
	}

	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction direction, IPlantable plantable){
		return plant.getBlock() == plantable;
	}

	@Override
	public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos){
		return true;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random){
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
