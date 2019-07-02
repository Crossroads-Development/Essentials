package com.Da_Technomancer.essentials.blocks.redstone;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashSet;

public class DefaultRedstoneHandler implements IRedstoneHandler{

	@Override
	public void updateRedstone(LazyOptional<IRedstoneHandler> src, int dist, HashSet<BlockPos> visited){

	}

	@Override
	public void summonTrigger(int dist, HashSet<BlockPos> visited){

	}
}
