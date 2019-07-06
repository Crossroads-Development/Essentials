package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;

import java.lang.ref.WeakReference;

public interface IRedstoneHandler{

	/**
	 * @return The current redstone output
	 */
	public float getOutput();

	/**
	 * Finds and adds dependent circuitry (circuits that use the signal from this circuit)
	 * @param src The source
	 * @param dist The distance in blocks travelled. Must be below RedstoneUtil.getMaxRange()
	 * @param fromSide The side this is receiving from
	 * @param nominalSide The output side of the original calling circuit
	 */
	public void findDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide);

	/**
	 * Finds and adds source circuity (circuits whose output this circuit uses)
	 * @param dependency The dependent
	 * @param dist The distance in blocks travelled. Must be below RedstoneUtil.getMaxRange()
	 * @param toSide The side this is outputting on
	 * @param nominalSide The input side of the original calling circuit
	 */
	public void requestSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide);

	/**
	 * Adds an external circuit as a source (a circuit whose output this circuit uses)
	 * @param src The source
	 * @param fromSide The side this circuit is receiving from
	 */
	public void addSrc(WeakReference<LazyOptional<IRedstoneHandler>> src, Direction fromSide);

	/**
	 * Adds an external circuit as a dependent (a circuit that uses this circuit's output)
	 * @param dependent The dependent
	 * @param toSide The side this circuit is outputting on
	 */
	public void addDependent(WeakReference<LazyOptional<IRedstoneHandler>> dependent, Direction toSide);

	/**
	 * Notifies of a change in the result of a getPower() call on a source
	 * @param src A linked source
	 */
	public void notifyInputChange(WeakReference<LazyOptional<IRedstoneHandler>> src);
}
