package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
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

public class BasicItemSplitter extends AbstractSplitter{

	protected BasicItemSplitter(String name, Properties prop){
		super(name, prop);
	}

	public BasicItemSplitter(){
		super("basic_item_splitter", ESBlocks.getMetalProperty());
	}

	@Override
	protected boolean isBasic(){
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new BasicItemSplitterTileEntity(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.item_splitter_basic"));
		tooltip.add(Component.translatable("tt.essentials.basic_item_splitter_formula"));
		tooltip.add(Component.translatable("tt.essentials.item_splitter_chute"));
	}

	@Override
	protected Component getModeComponent(AbstractSplitterTE te, int newMode){
		return Component.translatable("tt.essentials.basic_item_splitter.mode", newMode, te.getDistribution().base);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
		return ITickableTileEntity.createTicker(type, BasicItemSplitterTileEntity.TYPE);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof Container cont && newState.getBlock() != state.getBlock()){
			Containers.dropContents(worldIn, pos, cont);
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}
}
