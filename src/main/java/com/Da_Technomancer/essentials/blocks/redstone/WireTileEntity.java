package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.redstone.IRedstoneCapable;
import com.Da_Technomancer.essentials.api.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashSet;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.wireCircuit;

public class WireTileEntity extends BlockEntity implements IRedstoneCapable{

	public static final BlockEntityType<WireTileEntity> TYPE = ESTileEntity.createType(WireTileEntity::new, wireCircuit);

	public long lastUpdateTime;
	protected final RedsHandler redsHandler = createRedsHandler();

	protected WireTileEntity(BlockEntityType<? extends WireTileEntity> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}

	public WireTileEntity(BlockPos pos, BlockState state){
		this(TYPE, pos, state);
	}

	protected RedsHandler createRedsHandler(){
		return new RedsHandler();
	}

	@Nullable
	@Override
	public IRedstoneHandler getRedstoneHandler(Direction dir){
		if(dir == null || dir.getAxis() != Direction.Axis.Y){
			return redsHandler;
		}
		return null;
	}

	protected class RedsHandler implements IRedstoneHandler{

		@Override
		public boolean isInvalid(){
			return isRemoved();
		}

		@Override
		public float getOutput(){
			return 0;
		}

		@Override
		public boolean shouldMuteOutput(){
			return true;
		}

		@Override
		public void findDependents(IRedstoneHandler src, int dist, Direction fromSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(worldPosition);
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != fromSide){
					IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), dir.getOpposite());
					if(handler != null){
						if(handler instanceof RedsHandler otherRedsHandler){
							otherRedsHandler.routeDependents(src, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.findDependents(src, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		//A more efficient routing algorithm that is used in place of the stricter API when going between wires, which can be expected to be well behaved
		protected void routeDependents(IRedstoneHandler src, int dist, Direction fromSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(worldPosition) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != fromSide){
					IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), dir.getOpposite());
					if(handler != null){
						if(handler instanceof RedsHandler otherRedsHandler){
							otherRedsHandler.routeDependents(src, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.findDependents(src, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		@Override
		public void requestSrc(IRedstoneHandler dependency, int dist, Direction toSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(worldPosition);
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != toSide){
					IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), dir.getOpposite());
					if(handler != null){
						if(handler instanceof RedsHandler otherRedsHandler){
							otherRedsHandler.routeSrc(dependency, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.requestSrc(dependency, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		//A more efficient routing algorithm that is used in place of the stricter API when going between wires, which can be expected to be well behaved
		protected void routeSrc(IRedstoneHandler dependency, int dist, Direction toSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(worldPosition) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}
			for(Direction dir : Direction.Plane.HORIZONTAL){
				if(dir != toSide){
					IRedstoneHandler handler = level.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, worldPosition.relative(dir), dir.getOpposite());
					if(handler != null){
						if(handler instanceof RedsHandler otherRedsHandler){
							otherRedsHandler.routeSrc(dependency, dist, dir.getOpposite(), nominalSide, visited);
						}else{
							handler.requestSrc(dependency, dist, dir.getOpposite(), nominalSide);
						}
					}
				}
			}
		}

		@Override
		public void addSrc(IRedstoneHandler src, Direction fromSide){

		}

		@Override
		public void addDependent(IRedstoneHandler dependent, Direction toSide){

		}

		@Override
		public void notifyInputChange(IRedstoneHandler src){

		}
	}
}
