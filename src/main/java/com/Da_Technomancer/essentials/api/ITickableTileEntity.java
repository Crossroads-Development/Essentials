package com.Da_Technomancer.essentials.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface ITickableTileEntity{

	@Nullable
	static <T extends BlockEntity> BlockEntityTicker<T> createTicker(@Nullable BlockEntityType<T> actualType, BlockEntityType<?> requiredType){
		return requiredType == actualType ? new GeneralTileEntityTicker<T>() : null;
	}

	default void tick(){

	}

	default void clientTick(){
		tick();
	}

	default void serverTick(){
		tick();
	}

	class GeneralTileEntityTicker<T extends BlockEntity> implements BlockEntityTicker<T>{

		@Override
		public void tick(Level world, BlockPos pos, BlockState state, T te){
			if(te instanceof ITickableTileEntity ite){
				if(world.isClientSide){
					ite.clientTick();
				}else{
					ite.serverTick();
				}
			}
		}
	}
}
