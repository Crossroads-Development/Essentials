package com.Da_Technomancer.essentials;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class is for holding a list of changes being made to a world, but that haven't actually happened yet.
 * It is able to return the blockstate for a given position as if these changes actually happened. Finally it can do all the saved changes. 
 * This is for making large changes to the world step by step without a huge number of unnecessary intermediate blocks updates.
 */
public class LevelBuffer implements IBlockReader{
	
	private final Level world;
	private final HashMap<BlockPos, BlockState> memory = new HashMap<>();
	
	public LevelBuffer(Level worldObj){
		this.world = worldObj;
	}
	
	public Level getLevel(){
		return world;
	}
	
	public void addChange(BlockPos pos, BlockState state){
		pos = pos.immutable();
		if(getBlockState(pos) == state){
			return;
		}
		memory.put(pos, state);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos){
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos){
		if(memory.containsKey(pos)){
			return memory.get(pos);
		}
		
		return world.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos){
		return getBlockState(pos).getFluidState();
	}

	public void applyChanges(int flags){
		for(Entry<BlockPos, BlockState> ent : memory.entrySet()){
			if(world.getBlockState(ent.getKey()) != ent.getValue()){
				world.setBlock(ent.getKey(), ent.getValue(), flags);
			}
		}
		memory.clear();
	}

	public Set<BlockPos> changedPositions(){
		return memory.keySet();
	}
}
