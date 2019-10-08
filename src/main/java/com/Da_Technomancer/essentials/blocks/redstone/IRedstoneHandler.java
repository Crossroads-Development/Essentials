package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;

import java.lang.ref.WeakReference;

/**
 * Handles circuitry signals for both receiving and emitting.
 * Most logic circuits are both emitters and receivers
 * Most devices that interact with circuits are only emitters or receivers
 */
public interface IRedstoneHandler{

	/**
	 * Functioning implementation only required for emitters
	 *
	 * @return The current redstone output
	 */
	public float getOutput();

	/**
	 * Finds and adds dependent circuitry (circuits that use the signal from the calling circuit)
	 * Called by circuits "upstream"
	 *
	 * Functioning implementation only required for receivers
	 *
	 * @param src The source
	 * @param dist The distance in blocks travelled. Must be below RedstoneUtil.getMaxRange()
	 * @param fromSide The side this is receiving from
	 * @param nominalSide The output side of the original calling circuit
	 */
	public void findDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide);

	/**
	 * Finds and adds source circuity (circuits whose output is used by the calling circuit)
	 * Called by circuits "downstream"
	 *
	 * Functioning implementation only required for emitters
	 *
	 * @param dependency The dependent
	 * @param dist The distance in blocks travelled. Must be below RedstoneUtil.getMaxRange()
	 * @param toSide The side this is outputting on
	 * @param nominalSide The input side of the original calling circuit
	 */
	public void requestSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide);

	/**
	 * Adds an external circuit as a source (a circuit whose output this circuit uses)
	 *
	 * Functioning implementation only required for receivers
	 *
	 * @param src The source
	 * @param fromSide The side this circuit is receiving from
	 */
	public void addSrc(WeakReference<LazyOptional<IRedstoneHandler>> src, Direction fromSide);

	/**
	 * Adds an external circuit as a dependent (a circuit that uses this circuit's output)
	 *
	 * Functioning implementation only required for emitters
	 *
	 * @param dependent The dependent
	 * @param toSide The side this circuit is outputting on
	 */
	public void addDependent(WeakReference<LazyOptional<IRedstoneHandler>> dependent, Direction toSide);

	/**
	 * Notifies of a change in the result of a getPower() call on a source
	 *
	 * Functioning implementation only required for receivers
	 *
	 * @param src A linked source
	 */
	public void notifyInputChange(WeakReference<LazyOptional<IRedstoneHandler>> src);
}
