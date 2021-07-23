package com.Da_Technomancer.essentials.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface ITickableTileEntity{

	@Nullable
	@SuppressWarnings("unchecked")
	static <T extends BlockEntity, A extends BlockEntity & ITickableTileEntity> BlockEntityTicker<T> createTicker(@Nullable BlockEntityType<T> actualType, BlockEntityType<A> requiredType){
		return requiredType == actualType ? (BlockEntityTicker<T>) new GeneralTileEntityTicker<A>() : null;
	}

	default void tick(){

	}

	default void clientTick(){
		tick();
	}

	default void serverTick(){
		tick();
	}

	class GeneralTileEntityTicker<T extends BlockEntity & ITickableTileEntity> implements BlockEntityTicker<T>{

		@Override
		public void tick(Level world, BlockPos pos, BlockState state, T te){
			if(world.isClientSide){
				te.clientTick();
			}else{
				te.serverTick();
			}
		}
	}
}
