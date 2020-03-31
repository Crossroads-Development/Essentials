package com.Da_Technomancer.essentials;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class is for holding a list of changes being made to a world, but that haven't actually happened yet.
 * It is able to return the blockstate for a given position as if these changes actually happened. Finally it can do all the saved changes. 
 * This is for making large changes to the world step by step without a huge number of unnecessary intermediate blocks updates.
 */
public class WorldBuffer implements IBlockReader{
	
	private final World worldObj;
	private final HashMap<BlockPos, BlockState> memory = new HashMap<>();
	
	public WorldBuffer(World worldObj){
		this.worldObj = worldObj;
	}
	
	public World getWorld(){
		return worldObj;
	}
	
	public void addChange(BlockPos pos, BlockState state){
		pos = pos.toImmutable();
		if(getBlockState(pos) == state){
			return;
		}
		memory.put(pos, state);
	}

	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos){
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos){
		if(memory.containsKey(pos)){
			return memory.get(pos);
		}
		
		return worldObj.getBlockState(pos);
	}

	@Override
	public IFluidState getFluidState(BlockPos pos){
		return Fluids.EMPTY.getDefaultState();//TODO fluid support when forge is ready
	}

	public void applyChanges(int flags){
		for(Entry<BlockPos, BlockState> ent : memory.entrySet()){
			if(worldObj.getBlockState(ent.getKey()) != ent.getValue()){
				worldObj.setBlockState(ent.getKey(), ent.getValue(), flags);
			}
		}
		memory.clear();
	}

	public Set<BlockPos> changedPositions(){
		return memory.keySet();
	}
}
