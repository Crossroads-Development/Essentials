package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.util.TriState;

import java.util.List;

public class FertileSoil extends Block{

	private static final TagKey<Block> PLANT_WHITELIST = BlockTags.create(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, "fertile_soil_whitelist"));
	private static final TagKey<Block> PLANT_BLACKLIST = BlockTags.create(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, "fertile_soil_blacklist"));

	/**
	 * Fertile soil can be made to work on any block type by adding it to the essentials:blocks/fertile_soil_whitelist tag
	 * Any block can be removed from fertile soil by adding it to the essentials:blocks/fertile_soil_blacklist tag
	 * The blacklist overrides the whitelist
	 */
	private boolean appliesToPlant(BlockState plantState){
		return (plantState.getBlock() instanceof BonemealableBlock || plantState.is(PLANT_WHITELIST)) && !plantState.is(PLANT_BLACKLIST);
	}

	protected FertileSoil(){
		super(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL).randomTicks());
		String name = "fertile_soil";
		ESBlocks.queueForRegister(name, this);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.fertile_soil.desc"));
		tooltip.add(Component.translatable("tt.essentials.fertile_soil.benefits"));
	}

	@Override
	public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos soilPos, Direction direction, BlockState plant){
		return appliesToPlant(plant) ? TriState.TRUE : TriState.DEFAULT;
	}

	@Override
	public boolean isFertile(BlockState state, BlockGetter world, BlockPos pos){
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random){
		if(ESConfig.fertileSoilRate.get() < 100D * Math.random()){
			return;
		}

		BlockPos upPos = pos.relative(Direction.UP);
		BlockState upState = worldIn.getBlockState(upPos);
		if(appliesToPlant(upState)){
			BlockState stateToPlace = upState.getBlock().defaultBlockState();
			for(int i = 0; i < 4; i++){
				Direction offset = Direction.from2DDataValue(i);
				BlockPos offsetPos = upPos.relative(offset);
				if(worldIn.getBlockState(offsetPos).isAir() && stateToPlace.canSurvive(worldIn, offsetPos)){
					worldIn.setBlockAndUpdate(offsetPos, stateToPlace);
				}
			}
		}
	}

	@Override
	protected MapCodec<? extends Block> codec(){
		return ESBlocks.FERTILE_SOIL_TYPE.value();
	}
}
