package com.Da_Technomancer.essentials.api.redstone;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wires will connect to any block extending this interface if canConnect returns true
 */
public interface IWireConnect{

	boolean canConnect(Direction side, BlockState state);

	default Block wireAsBlock(){
		return (Block) this;
	}
}
