package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockPlaceContext ;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateDefinition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;
import net.minecraft.world.server.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.block.AbstractBlock.Properties;

public class AnalogLamp extends Block{

	protected AnalogLamp(){
		super(Properties.of(Material.BUILDABLE_GLASS).lightLevel(state -> state.getValue(ESProperties.REDSTONE)).strength(0.3F).sound(SoundType.GLASS).isValidSpawn(AnalogLamp::propagateFunction));
		String name = "analog_lamp";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	private static Boolean propagateFunction(BlockState state, IBlockReader world, BlockPos pos, EntityType<?> type){
		return Boolean.TRUE;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext  context){
		return defaultBlockState().setValue(ESProperties.REDSTONE, RedstoneUtil.getRedstoneAtPos(context.getLevel(), context.getClickedPos()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.analog_lamp.desc"));
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
