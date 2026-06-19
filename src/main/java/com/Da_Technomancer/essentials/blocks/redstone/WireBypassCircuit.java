package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;
import java.util.List;

public class WireBypassCircuit extends GenericWireBypass{

	public WireBypassCircuit(){
		super("wire_bypass_circuit");
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec(){
		return ESBlocks.WIRE_BYPASS_CIRCUIT_TYPE.value();
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(ESProperties.HORIZ_ORIENT, context.getHorizontalDirection().getAxis());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.HORIZ_ORIENT);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new WireBypassTileEntity(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.wire_bypass_circuit"));
	}

	@Override
	public Direction[] getMappedDirection(Direction fromDirection, BlockState state){
		if(state.hasProperty(ESProperties.HORIZ_ORIENT)){
			Direction.Axis orientation = state.getValue(ESProperties.HORIZ_ORIENT);
			return new Direction[] {fromDirection.getAxis() == orientation ? fromDirection.getCounterClockWise() : fromDirection.getClockWise()};
		}
		return new Direction[0];
	}
}
