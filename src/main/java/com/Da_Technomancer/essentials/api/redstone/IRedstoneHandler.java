package com.Da_Technomancer.essentials.api.redstone;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

/**
 * Handles circuitry signals for both receiving and emitting.
 * Most logic circuits are both emitters and receivers
 * Most devices that interact with circuits are only emitters or receivers
 */
public interface IRedstoneHandler{

	public static final BlockCapability<IRedstoneHandler, Direction> REDS_HANDLER_BLOCK = BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, "redstone_handler"), IRedstoneHandler.class);

	/**
	 * Functioning implementation only required for emitters
	 *
	 * @return The current redstone output
	 */
	float getOutput();

	/**
	 * Indicates that getOutput() has a dummy implementation and does not give useful information
	 * @return Whether to pretend this handler doesn't implement getOutput()
	 */
	default boolean shouldMuteOutput(){
		return false;
	}

	/**
	 * Should be true when this block has unloaded or been removed
	 * @return Whether this handler should no longer be interacted with or kept in caches
	 */
	boolean isInvalid();

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
	void findDependents(IRedstoneHandler src, int dist, Direction fromSide, Direction nominalSide);

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
	void requestSrc(IRedstoneHandler dependency, int dist, Direction toSide, Direction nominalSide);

	/**
	 * Adds an external circuit as a source (a circuit whose output this circuit uses)
	 *
	 * Functioning implementation only required for receivers
	 *
	 * @param src The source
	 * @param fromSide The side this circuit is receiving from
	 */
	void addSrc(IRedstoneHandler src, Direction fromSide);

	/**
	 * Adds an external circuit as a dependent (a circuit that uses this circuit's output)
	 *
	 * Functioning implementation only required for emitters
	 *
	 * @param dependent The dependent
	 * @param toSide The side this circuit is outputting on
	 */
	void addDependent(IRedstoneHandler dependent, Direction toSide);

	/**
	 * Notifies of a change in the result of a getPower() call on a source
	 *
	 * Functioning implementation only required for receivers
	 *
	 * @param src A linked source
	 */
	void notifyInputChange(IRedstoneHandler src);
}
