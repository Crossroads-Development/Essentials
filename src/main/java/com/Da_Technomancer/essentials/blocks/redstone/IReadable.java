package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Any block can implement this interface to be readable by Reader Circuits (Circuit version of a comparator)
 */
public interface IReadable{

	/**
	 * @param world World
	 * @param pos The position of the block
	 * @param state The current state of this block in the world
	 * @return The value to be read by a Reader Circuit. Return value will be rejected if negative
	 */
	public float read(World world, BlockPos pos, BlockState state);
}
