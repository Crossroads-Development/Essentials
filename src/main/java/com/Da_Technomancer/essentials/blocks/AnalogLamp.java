package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;
import java.util.List;

public class AnalogLamp extends Block{

	protected AnalogLamp(){
		super(Properties.of(Material.BUILDABLE_GLASS).lightLevel(state -> state.getValue(ESProperties.REDSTONE)).strength(0.3F).sound(SoundType.GLASS).isValidSpawn(AnalogLamp::propagateFunction));
		String name = "analog_lamp";
		ESBlocks.queueForRegister(name, this);
	}

	private static Boolean propagateFunction(BlockState state, BlockGetter world, BlockPos pos, EntityType<?> type){
		return Boolean.TRUE;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(ESProperties.REDSTONE, RedstoneUtil.getRedstoneAtPos(context.getLevel(), context.getClickedPos()));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.analog_lamp.desc"));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.REDSTONE);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		if(!worldIn.isClientSide){
			int current = state.getValue(ESProperties.REDSTONE);
			int worldReds = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
			if(current != worldReds){
				if(worldReds == 0){
					//Turn off w/ 4 tick delay (2 redstone ticks), (vanilla redstone lamp behaviour reproduced here)
					worldIn.scheduleTick(pos, this, 4);
				}else{
					//Turn on/change light level instantly
					worldIn.setBlock(pos, state.setValue(ESProperties.REDSTONE, worldReds), 2);
				}
			}

		}
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand){
		int current = state.getValue(ESProperties.REDSTONE);
		int worldReds = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
		if(current != worldReds){
			worldIn.setBlock(pos, state.setValue(ESProperties.REDSTONE, worldReds), 2);
		}
	}
}
