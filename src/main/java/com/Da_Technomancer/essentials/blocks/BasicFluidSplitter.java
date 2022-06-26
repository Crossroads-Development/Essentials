package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class BasicFluidSplitter extends AbstractSplitter{

	protected BasicFluidSplitter(String name, Properties prop){
		super(name, prop);
	}

	public BasicFluidSplitter(){
		super("basic_fluid_splitter", ESBlocks.getMetalProperty());
	}

	@Override
	protected boolean isBasic(){
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new BasicFluidSplitterTileEntity(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.fluid_splitter_basic"));
		tooltip.add(Component.translatable("tt.essentials.basic_fluid_splitter_formula"));
		tooltip.add(Component.translatable("tt.essentials.fluid_splitter_chute"));
	}

	@Override
	protected Component getModeComponent(AbstractSplitterTE te, int newMode){
		return Component.translatable("tt.essentials.basic_fluid_splitter.mode", newMode, te.getDistribution().base);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
		return ITickableTileEntity.createTicker(type, BasicFluidSplitterTileEntity.TYPE);
	}
}
