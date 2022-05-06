package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class FertileSoil extends Block{

	private final BlockState plant;
	private final SeedCategory category;

	protected FertileSoil(String plantName, BlockState plant, SeedCategory category){
		super(BlockBehaviour.Properties.of(category == SeedCategory.HELL_CROP ? Material.SAND : Material.DIRT).strength(0.5F).sound(SoundType.GRAVEL).randomTicks());
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
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.fertile_soil.desc"));
		tooltip.add(new TranslatableComponent("tt.essentials.fertile_soil.benefits"));
		if(category == SeedCategory.HELL_CROP){
			tooltip.add(new TranslatableComponent("tt.essentials.fertile_soil.quip").setStyle(ESConfig.TT_QUIP));//MCP note: setStyle
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
