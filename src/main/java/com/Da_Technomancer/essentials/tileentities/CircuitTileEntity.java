package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractCircuit;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.google.common.annotations.VisibleForTesting;
import jdk.internal.jline.internal.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TickPriority;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

public class CircuitTileEntity extends TileEntity{

	@ObjectHolder(Essentials.MODID + ":circuit")
	private static TileEntityType<CircuitTileEntity> TYPE = null;

	/**
	 * Used primarily to workaround a forge issue involving getBlockState while loading
	 */
	private boolean[] useInput = new boolean[3];

	@SuppressWarnings("unchecked")
	private ArrayList<LazyOptional<IRedstoneHandler>>[] inputs = new ArrayList[] {new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0)};
	@VisibleForTesting//TODO
	public float[] inputStr = new float[3];

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

	private Direction calcOrient(int index){
		return Direction.byHorizontalIndex((1 + index + getFacing().getHorizontalIndex()) & 3);
	}

	public static int calcOrientIndex(Direction dir, Direction blockOrient){
		int dirIndex = 3 + dir.getHorizontalIndex() - blockOrient.getHorizontalIndex();
		return dirIndex &= 3;
	}

	public void recalculateOutput(){
		float prevOut = outHandler.getOutput();
		for(int i = 0; i < 3; i++){
			inputStr[i] = RedstoneUtil.calcInput(inputs[i], world, pos, calcOrient(i));
		}
		float newOut = outHandler.getOutput();
		if(RedstoneUtil.didChange(prevOut, newOut)){
			outHandler.informListeners(newOut);
		}
		if(RedstoneUtil.clampToVanilla(newOut) != RedstoneUtil.clampToVanilla(prevOut)){
			Direction facing = getFacing();
			world.neighborChanged(pos.offset(facing), getOwner(), pos);
		}
		markDirty();
	}

	public void addBlock(){
		AbstractCircuit own = getOwner();
		for(int i = 0; i < 3; i++){
			if(own.useInput(i)){
				useInput[i] = true;
				Direction orient = calcOrient(i);
				TileEntity te = world.getTileEntity(pos.offset(orient));
				LazyOptional<IRedstoneHandler> otherHandler;
				if(te != null && (otherHandler = te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, orient.getOpposite())).isPresent()){
					otherHandler.orElseThrow(NullPointerException::new).summonTrigger(0, new HashSet<>(4));
				}
			}
		}
		world.getPendingBlockTicks().scheduleTick(pos, own, RedstoneUtil.DELAY, TickPriority.HIGH);
	}

	public void wipeCache(){
		outOptional.invalidate();
		outHandler.informListeners(0);
		outHandler.listeners.clear();
		outOptional = LazyOptional.of(() -> outHandler);

		for(int i = 0; i < 3; i++){
			LazyOptional<InHandler> prev = inOptionals[i];
			//Re-use the InHandler instance in the old LazyOptional
			NonNullSupplier<InHandler> sup = () -> prev.orElseThrow(NullPointerException::new);
			inOptionals[i] = LazyOptional.of(sup);
			prev.invalidate();
		}

		updateContainingBlockInfo();
		for(int i = 0; i < 3; i++){
			inputs[i].clear();
			inputStr[i] = 0;
			useInput[i] = false;
		}
		addBlock();

		markDirty();
	}

	@Override
	public void remove(){
		super.remove();
		outHandler.informListeners(0);
		outOptional.invalidate();
		for(int i = 0; i < 3; i++){
			inOptionals[i].invalidate();
		}
	}

	@Override
	public void onLoad(){
		super.onLoad();
		//Due to a bug/change in Forge (Forge issue #5883), using getBlockState during onLoad will break the game
		//The following code is a little weird to avoid using getBlockState
		for(int i = 0; i < 3; i++){
			if(useInput[i]){
				Direction orient = calcOrient(i);
				TileEntity te = world.getTileEntity(pos.offset(orient));
				LazyOptional<IRedstoneHandler> otherHandler;
				if(te != null && (otherHandler = te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, orient.getOpposite())).isPresent()){
					otherHandler.orElseThrow(NullPointerException::new).summonTrigger(0, new HashSet<>(4));
				}
			}
		}
		//Use Blocks.STONE instead of the real block to avoid using getBlockState.
		world.getPendingBlockTicks().scheduleTick(pos, Blocks.STONE, RedstoneUtil.DELAY, TickPriority.HIGH);
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);
		for(int i = 0; i < 3; i++){
			inputStr[i] = nbt.getFloat("str_" + i);
			useInput[i] = nbt.getBoolean("inp_" + i);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		for(int i = 0; i < 3; i++){
			nbt.putFloat("str_" + i, inputStr[i]);
			nbt.putBoolean("inp_" + i, useInput[i]);
		}
		return nbt;
	}

	public final OutHandler outHandler = new OutHandler();
	private LazyOptional<IRedstoneHandler> outOptional = LazyOptional.of(() -> outHandler);
	@SuppressWarnings("unchecked")
	private final LazyOptional<InHandler>[] inOptionals = new LazyOptional[] {LazyOptional.of(() -> new InHandler(0)), LazyOptional.of(() -> new InHandler(1)), LazyOptional.of(() -> new InHandler(2))};

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
		if(cap == RedstoneUtil.REDSTONE_CAPABILITY){
			Direction dir = getFacing();
			if(side == null || dir == null || side.getAxis() == Direction.Axis.Y){
				return super.getCapability(cap, side);
			}
			if(dir == side){
				return (LazyOptional<T>) outOptional;
			}
			AbstractCircuit cir = getOwner();
			int dirIndex = calcOrientIndex(side, dir);
			if(cir == null || !useInput[dirIndex]){
				return super.getCapability(cap, side);
			}
			return (LazyOptional<T>) inOptionals[dirIndex];
		}
		return super.getCapability(cap, side);
	}

	private class InHandler implements IRedstoneHandler{

		private final int side;
		private final Listener listener = new Listener();

		private InHandler(int side){
			this.side = side;
		}

		@Override
		public void updateRedstone(LazyOptional<IRedstoneHandler> src, int dist, HashSet<BlockPos> visited){
			inputs[side].add(src);
			if(src.isPresent()){
				src.orElseThrow(NullPointerException::new).listen(new WeakReference<>(listener));
			}
			world.getPendingBlockTicks().scheduleTick(pos, getOwner(), RedstoneUtil.DELAY, TickPriority.HIGH);
		}

		@Override
		public void summonTrigger(int dist, HashSet<BlockPos> visited){

		}

		private class Listener implements Consumer<Float>{

			@Override
			public void accept(Float newStren){
				world.getPendingBlockTicks().scheduleTick(pos, getOwner(), RedstoneUtil.DELAY, TickPriority.HIGH);
			}
		}
	}

	public class OutHandler implements IRedstoneHandler{

		private ArrayList<WeakReference<Consumer<Float>>> listeners = new ArrayList<>(1);

		private void informListeners(float newValue){
			for(int i = 0; i < listeners.size(); i++){
				Consumer<Float> lis = null;
				if(listeners.get(i) == null || (lis = listeners.get(i).get()) == null){
					listeners.remove(i);
					i--;
				}else{
					lis.accept(newValue);
				}
			}
		}

		@Override
		public void updateRedstone(LazyOptional<IRedstoneHandler> src, int dist, HashSet<BlockPos> visited){

		}

		@Override
		public void summonTrigger(int dist, HashSet<BlockPos> visited){
			listeners.clear();
			visited.add(pos);
			Direction dir = getFacing();
			TileEntity te = world.getTileEntity(pos.offset(dir));
			LazyOptional<IRedstoneHandler> handler;
			if(te != null && (handler = te.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, dir.getOpposite())).isPresent()){
				handler.orElseThrow(NullPointerException::new).updateRedstone(outOptional, 0, new HashSet<>(visited.size()));
			}
		}

		@Override
		public float getOutput(){
			if(removed){
				return 0;
			}
			AbstractCircuit cir = getOwner();
			if(cir == null){
				return 0;
			}
			return cir.getOutput(inputStr[0], inputStr[1], inputStr[2], CircuitTileEntity.this);
		}

		@Override
		public void listen(WeakReference<Consumer<Float>> listener){
			listeners.add(listener);
		}
	}
}
