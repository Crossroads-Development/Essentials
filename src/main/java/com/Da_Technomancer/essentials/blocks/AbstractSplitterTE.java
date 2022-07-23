package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSplitterTE extends BlockEntity implements ITickableTileEntity{

	protected int mode = 6;
	protected final BlockPos[] endPos = new BlockPos[2];

	protected AbstractSplitterTE(BlockEntityType<? extends AbstractSplitterTE> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}

	public SplitDistribution getDistribution(){
		return SplitDistribution.TWELVE;
	}

	protected Direction getFacing(){
		BlockState state = getBlockState();
		if(!state.hasProperty(ESProperties.FACING)){
			return Direction.DOWN;
		}
		return state.getValue(ESProperties.FACING);
	}

	public int getMode(){
		return mode;
	}

	public int increaseMode(){
		mode++;
		mode %= getDistribution().maxMode();
		setChanged();
		return mode;
	}

	public void refreshCache(){
		Direction dir = getFacing();
		int maxChutes = ESConfig.itemChuteRange.get();

		for(int i = 0; i < 2; i++){
			int extension;

			for(extension = 1; extension <= maxChutes; extension++){
				BlockState target = level.getBlockState(worldPosition.relative(dir, extension));
				if(target.getBlock() != ESBlocks.itemChute || target.getValue(ESProperties.AXIS) != dir.getAxis()){
					break;
				}
			}

			endPos[i] = worldPosition.relative(dir, extension);
			dir = dir.getOpposite();
		}
	}

	public enum SplitDistribution{

		TWELVE(12, new int[] {
				0b000000000000, // 0/1
				0b000000000001, // 1/12
				0b000001000001, // 1/6
				0b000100010001, // 1/4
				0b001001001001, // 1/3
				0b101001001001, // 5/12
				0b010101010101, // 1/2
				0b110101010101, // 7/12
				0b101101101101, // 2/3
				0b011101110111, // 3/4
				0b111101110111, // 5/6
				0b011111111111, // 11/12
				0b111111111111  // 1/1
		}),
		FIFTEEN(15, new int[] {
				0b000000000000000, // 0/1
				0b000000000000001, // 1/15
				0b000000010000001, // 2/15
				0b000010000100001, // 1/5
				0b000100010001001, // 4/15
				0b001001001001001, // 1/3
				0b001010010100101, // 2/5
				0b010101010101010, // 7/15
				0b101010101010101, // 8/15
				0b101011010110101, // 3/5
				0b101101101101101, // 2/3
				0b011101110111011, // 11/15
				0b011110111101111, // 4/5
				0b011111101111111, // 13/15
				0b011111111111111, // 14/15
				0b111111111111111  // 1/1
		});

		public final int base;
		private final int[] patterns;

		SplitDistribution(int base, int[] patterns){
			this.base = base;
			this.patterns = patterns;
		}

		public int maxMode(){
			return patterns.length;
		}

		public boolean shouldDispense(int mode, int stage){
			stage %= base;
			return ((patterns[mode % patterns.length] >>> stage) & 0x1) == 1;
		}
	}
}
