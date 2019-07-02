package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.function.Consumer;

public interface IRedstoneHandler{

	/**
	 * Adds a new source to all connected devices, or notifies of a change
	 * @param src The original redstone source
	 * @param dist The distance travelled
	 * @param visited A set of all visited positions, for more efficient routing only
	 */
	public void updateRedstone(LazyOptional<IRedstoneHandler> src, int dist, HashSet<BlockPos> visited);

	/**
	 * Queries all connected sources, requesting an updateRedstone call for a new device
	 * Forgets all previous listeners (listeners should re-register when the new updateRedstone call occurs)
	 * @param dist Distance travelled
	 * @param visited A set of all visited positions, for more efficient routing only
	 */
	public void summonTrigger(int dist, HashSet<BlockPos> visited);

	/**
	 * Gets the output if this device acts as a source
	 * @return The output if this is a source, zero otherwise
	 */
	public default float getOutput(){
		return 0;
	};

	/**
	 * Registers a listener for when output changes, which will be called when getOutput changes value. Listeners will be forgotten if the capability is invalidated or summonTrigger is called
	 * @param listener The consumer that will be called with the new output
	 */
	public default void listen(WeakReference<Consumer<Float>> listener){

	}
}
