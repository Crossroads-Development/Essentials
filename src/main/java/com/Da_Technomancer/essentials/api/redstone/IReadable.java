package com.Da_Technomancer.essentials.api.redstone;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Any block can implement this interface to be readable by Reader Circuits (Circuit version of a comparator)
 * Or can register an instance of this interface with RedstoneUtil::registerReadable to avoid a hard dependency
 */
public interface IReadable{

	/**
	 * @param world World
	 * @param pos The position of the block
	 * @param state The current state of this block in the world
	 * @return The value to be read by a Reader Circuit.
	 */
	float read(Level world, BlockPos pos, BlockState state);
}
