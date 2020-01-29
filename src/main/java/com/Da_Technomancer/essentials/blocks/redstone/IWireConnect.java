package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

/**
 * Wires will connect to any block extending this interface if canConnect returns true
 */
public interface IWireConnect{

	boolean canConnect(Direction side, BlockState state);
}
