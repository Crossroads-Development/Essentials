package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class AnalogLamp extends Block{

	protected AnalogLamp(){
		super(Properties.of(Material.BUILDABLE_GLASS).lightLevel(state -> state.getValue(ESProperties.REDSTONE)).strength(0.3F).sound(SoundType.GLASS).isValidSpawn(AnalogLamp::propagateFunction));
		String name = "analog_lamp";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.analog_lamp.desc"));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.REDSTONE);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if(!worldIn.isClientSide) {
			int current = state.getValue(ESProperties.REDSTONE);
			int worldReds = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
			if(current != worldReds){
				if(worldReds == 0){
					//Turn off w/ 4 tick delay (2 redstone ticks), (vanilla redstone lamp behaviour reproduced here)
					worldIn.getBlockTicks().scheduleTick(pos, this, 4);
				}else{
					//Turn on/change light level instantly
					worldIn.setBlock(pos, state.setValue(ESProperties.REDSTONE, worldReds), 2);
				}
			}

		}
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand){
		int current = state.getValue(ESProperties.REDSTONE);
		int worldReds = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
		if(current != worldReds){
			worldIn.setBlock(pos, state.setValue(ESProperties.REDSTONE, worldReds), 2);
		}
	}
}
