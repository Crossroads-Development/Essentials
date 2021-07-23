package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

/**
 * Wires will connect to any block extending this interface if canConnect returns true
 */
public interface IWireConnect{

	boolean canConnect(Direction side, BlockState state);
}
