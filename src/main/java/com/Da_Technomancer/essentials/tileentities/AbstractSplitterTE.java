package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractSplitterTE extends TileEntity implements ITickableTileEntity{

	protected int mode = 6;
	protected final BlockPos[] endPos = new BlockPos[2];

	protected AbstractSplitterTE(TileEntityType<? extends AbstractSplitterTE> type){
		super(type);
	}

	protected abstract int[] getModes();

	protected int getActualMode(){
		return getModes()[mode];
	}

	public abstract int getBase();

	protected Direction getFacing(){
		BlockState state = getBlockState();
		if(!state.has(ESProperties.FACING)){
			return Direction.DOWN;
		}
		return state.get(ESProperties.FACING);
	}

	public int increaseMode(){
		mode++;
		mode %= getModes().length;
		markDirty();
		return mode;
	}

	public void refreshCache(){
		Direction dir = getFacing();
		int maxChutes = ESConfig.itemChuteRange.get();

		for(int i = 0; i < 2; i++){
			int extension;

			for(extension = 1; extension <= maxChutes; extension++){
				BlockState target = world.getBlockState(pos.offset(dir, extension));
				if(target.getBlock() != ESBlocks.itemChute || target.get(ESProperties.AXIS) != dir.getAxis()){
					break;
				}
			}

			endPos[i] = pos.offset(dir, extension);
			dir = dir.getOpposite();
		}
	}
}
