package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractCircuit;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import jdk.internal.jline.internal.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CircuitTileEntity extends TileEntity{

	@ObjectHolder(Essentials.MODID + ":circuit")
	private static TileEntityType<CircuitTileEntity> TYPE = null;

	public boolean builtConnections = false;
	private final ArrayList<WeakReference<LazyOptional<IRedstoneHandler>>> dependents = new ArrayList<>(1);
	private final ArrayList<Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Orient>> sources = new ArrayList<>(4);

	private LazyOptional<IRedstoneHandler> hanOptional = LazyOptional.of(RedsHandler::new);
	private WeakReference<LazyOptional<IRedstoneHandler>> hanReference = new WeakReference<>(hanOptional);

	private float output = 0;

	public CircuitTileEntity(){
		super(TYPE);
	}

	private AbstractCircuit getOwner(){
		Block b = getBlockState().getBlock();
		if(b instanceof AbstractCircuit){
			return (AbstractCircuit) b;
		}
		remove();
		return null;
	}

	private Direction getFacing(){
		BlockState s = getBlockState();
		if(s.has(EssentialsProperties.HORIZ_FACING)){
			return s.get(EssentialsProperties.HORIZ_FACING);
		}
		remove();
		return null;
	}

	public float getOutput(){
		buildConnections();
		return output;
	}

	/**
	 * Sets the current circuit output to the new power effective immediately, and performs necessary updates
	 * @param newPower The new output power
	 */
	private void setPower(float newPower){
		if(RedstoneUtil.didChange(output, newPower)){
			Direction facing = getFacing();

			/*
			if((output == 0) ^ (newPower == 0)){
				//Prevent a blocks update to reduce lag from frequent redstone changes
				world.setBlockState(pos, getBlockState().with(EssentialsProperties.REDSTONE_BOOL, newPower != 0), 2);
			}
			*/

			//If no dependents, assume we're outputting to vanilla redstone
			if(dependents.isEmpty() && RedstoneUtil.clampToVanilla(output) != RedstoneUtil.clampToVanilla(newPower)){
				output = newPower;
				world.neighborChanged(pos.offset(facing), getOwner(), pos.offset(facing.getOpposite()));
			}
			output = newPower;
			for(int i = 0; i < dependents.size(); i++){
				WeakReference<LazyOptional<IRedstoneHandler>> dependent = dependents.get(i);
				IRedstoneHandler handler;
				if(dependent == null || (handler = BlockUtil.get(dependent.get())) == null){
					//Entry is no longer valid- remove for faster future checks
					dependents.remove(i);
					i--;
					continue;
				}

				handler.notifyInputChange(hanReference);
			}

			markDirty();
		}
	}

	public void recalculateOutput(){
		buildConnections();//Can be needed when reloading

		float[] inputs = new float[3];
		boolean[] hasSrc = new boolean[3];

		for(int i = 0; i < sources.size(); i++){
			Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Orient> src = sources.get(i);
			WeakReference<LazyOptional<IRedstoneHandler>> ref;
			IRedstoneHandler handl;
			//Remove invalid entries to speed up future checks
			if(src == null || (ref = src.getLeft()) == null || (handl = BlockUtil.get(ref.get())) == null){
				sources.remove(i);
				i--;
				continue;
			}

			int ind = src.getRight().ordinal();
			if(ind > 2){
				IndexOutOfBoundsException e = new IndexOutOfBoundsException("Input into redstone device on the front! Pos: " + pos.toString() + "; Dim: " + world.dimension + "Type: " + getOwner().getRegistryName().toString());
				Essentials.logger.catching(e);
				//Invalid state- remove this input and skip
				sources.remove(i);
				i--;
				continue;
			}

			inputs[ind] = Math.max(inputs[ind], RedstoneUtil.sanitize(handl.getOutput()));
			hasSrc[ind] = true;
		}

		Direction facing = getFacing();

		AbstractCircuit owner = getOwner();

		//Any input without a circuit input uses vanilla redstone instead
		for(int i = 0; i < 3; i++){
			if(!hasSrc[i] && owner.useInput(Orient.values()[i])){
				Direction dir = Orient.values()[i].getFacing(facing);
				inputs[i] = RedstoneUtil.getRedstoneOnSide(world, pos, dir);
			}
		}

		float newOutput = owner.getOutput(inputs[0], inputs[1], inputs[2], this);
		setPower(RedstoneUtil.sanitize(newOutput));
	}

	public void buildConnections(){
		if(!builtConnections && !world.isRemote){
			builtConnections = true;
			dependents.clear();
			sources.clear();
			AbstractCircuit own = getOwner();
			Direction dir = getFacing();
			for(Orient or : Orient.INPUTS){
				if(own.useInput(or)){
					Direction checkDir = or.getFacing(dir);
					TileEntity te = world.getTileEntity(pos.offset(checkDir));
					IRedstoneHandler otherHandler;
					if(te != null && (otherHandler = BlockUtil.get(te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, checkDir.getOpposite()))) != null){
						otherHandler.requestSrc(hanReference, 0, checkDir.getOpposite(), checkDir);
					}
				}
			}

			TileEntity te = world.getTileEntity(pos.offset(dir));
			IRedstoneHandler otherHandler;
			if(te != null && (otherHandler = BlockUtil.get(te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite()))) != null){
				otherHandler.findDependents(hanReference, 0, dir.getOpposite(), dir);
			}

			world.getPendingBlockTicks().scheduleTick(pos, own, RedstoneUtil.DELAY, TickPriority.HIGH);
		}
	}

	public void wipeCache(){
		output = 0;
		builtConnections = false;
		dependents.clear();
		sources.clear();
		hanOptional.invalidate();
		hanOptional = LazyOptional.of(RedsHandler::new);
		hanReference = new WeakReference<>(hanOptional);
		updateContainingBlockInfo();
		buildConnections();

		markDirty();
	}

	@Override
	public void remove(){
		super.remove();
		hanOptional.invalidate();
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);
		output = nbt.getFloat("pow");
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putFloat("pow", output);
		return nbt;
	}


	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
		if(cap == RedstoneUtil.REDSTONE_CAPABILITY){
			Direction dir = getFacing();
			if(side == null || dir == null || side.getAxis() == Direction.Axis.Y){
				return super.getCapability(cap, side);
			}
			if(dir == side || getOwner().useInput(Orient.getOrient(side, dir))){
				return (LazyOptional<T>) hanOptional;
			}
		}
		return super.getCapability(cap, side);
	}
	
	private class RedsHandler implements IRedstoneHandler{

		@Override
		public float getOutput(){
			return output;
		}

		@Override
		public void findDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide){
			LazyOptional<IRedstoneHandler> srcOption = src.get();
			Orient or = Orient.getOrient(fromSide, getFacing());
			if(getOwner().useInput(or) && srcOption != null && srcOption.isPresent()){
				IRedstoneHandler srcHandler = BlockUtil.get(srcOption);
				srcHandler.addDependent(hanReference, nominalSide);
				Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Orient> toAdd = Pair.of(src, or);
				if(!sources.contains(toAdd)){
					sources.add(toAdd);
				}
			}
		}

		@Override
		public void requestSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide){
			LazyOptional<IRedstoneHandler> depenOption;
			if(Orient.getOrient(toSide, getFacing()) == Orient.FRONT && (depenOption = dependency.get()) != null && depenOption.isPresent()){
				IRedstoneHandler depHandler = BlockUtil.get(depenOption);
				depHandler.addSrc(hanReference, nominalSide);
				if(!dependents.contains(dependency)){
					dependents.add(dependency);
				}
			}
		}

		@Override
		public void addSrc(WeakReference<LazyOptional<IRedstoneHandler>> src, Direction fromSide){
			Orient or = Orient.getOrient(fromSide, getFacing());
			if(or != null && or != Orient.FRONT && getOwner().useInput(or)){
				Pair<WeakReference<LazyOptional<IRedstoneHandler>>, Orient> toAdd = Pair.of(src, or);
				if(!sources.contains(toAdd)){
					sources.add(toAdd);
					notifyInputChange(src);
				}
			}
		}

		@Override
		public void addDependent(WeakReference<LazyOptional<IRedstoneHandler>> dependent, Direction toSide){
			Orient or = Orient.getOrient(toSide, getFacing());
			if(or == Orient.FRONT && !dependents.contains(dependent)){
				dependents.add(dependent);
			}
		}

		@Override
		public void notifyInputChange(WeakReference<LazyOptional<IRedstoneHandler>> src){
			world.getPendingBlockTicks().scheduleTick(pos, getOwner(), RedstoneUtil.DELAY, TickPriority.HIGH);
		}
	}

	public enum Orient{

		CCW(),//Counter-clockwise of front (input)
		BACK(),//Rear input
		CW(),//Clockwise of front (input)
		FRONT();//Output

		public static final Orient[] INPUTS = {CCW, BACK, CW};

		public static Orient getOrient(Direction dir, Direction front){
			if(dir == front){
				return FRONT;
			}else if(dir.getOpposite() == front){
				return BACK;
			}else if(front.rotateY() == dir){
				return CW;
			}else if(front.rotateYCCW() == dir){
				return CCW;
			}else{
				throw new IllegalArgumentException("front &/or dir are vertical/null. Front: " + front + "; Dir: " + dir);
			}
		}

		public Direction getFacing(Direction front){
			switch(this){
				case FRONT:
					return front;
				case BACK:
					return front.getOpposite();
				case CW:
					return front.rotateY();
				case CCW:
					return front.rotateYCCW();
				default:
					throw new IllegalStateException("Unhandled Orientation: " + name());
			}
		}
	}
}
