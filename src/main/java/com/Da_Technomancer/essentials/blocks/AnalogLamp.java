package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class AnalogLamp extends Block{

	protected AnalogLamp(){
		super(Properties.create(Material.REDSTONE_LIGHT).setLightLevel(state -> state.get(ESProperties.REDSTONE)).hardnessAndResistance(0.3F).sound(SoundType.GLASS).setAllowsSpawn(AnalogLamp::propagateFunction));
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
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(ESProperties.REDSTONE, RedstoneUtil.getRedstoneAtPos(context.getWorld(), context.getPos()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.analog_lamp.desc"));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.REDSTONE);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if(!worldIn.isRemote) {
			int current = state.get(ESProperties.REDSTONE);
			int worldReds = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
			if(current != worldReds){
				if(worldReds == 0){
					//Turn off w/ 4 tick delay (2 redstone ticks), (vanilla redstone lamp behaviour reproduced here)
					worldIn.getPendingBlockTicks().scheduleTick(pos, this, 4);
				}else{
					//Turn on/change light level instantly
					worldIn.setBlockState(pos, state.with(ESProperties.REDSTONE, worldReds), 2);
				}
			}

		}
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand){
		int current = state.get(ESProperties.REDSTONE);
		int worldReds = RedstoneUtil.getRedstoneAtPos(worldIn, pos);
		if(current != worldReds){
			worldIn.setBlockState(pos, state.with(ESProperties.REDSTONE, worldReds), 2);
		}
	}
}
