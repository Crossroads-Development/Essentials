package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.TickPriority;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class AbstractTimeCircuitTileEntity extends CircuitTileEntity implements ITickableTileEntity{

	protected long ticksExisted = 0;
	protected long lastGameTick;
	protected final ArrayList<Pulse> queuedPulses = new ArrayList<>(2);


	public AbstractTimeCircuitTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state){
		super(type, pos, state);
	}

	public float calculatedOutput(){
		final long tickCountStandard = standardizedTickCount();
		float outputPower = 0;
		Iterator<Pulse> pulseIterator = queuedPulses.iterator();
		while(pulseIterator.hasNext()){
			Pulse pulse = pulseIterator.next();
			if(pulse.isExpired(tickCountStandard)){
				//Clean up expired pulses
				pulseIterator.remove();
			}else if(pulse.isActive(tickCountStandard)){
				outputPower = RedstoneUtil.chooseInput(outputPower, pulse.power);
			}
		}
		return outputPower;
	}

	@Override
	public void recalculateOutput(){
		super.recalculateOutput();
		updateScheduledTick();
	}

	protected void updateScheduledTick(){
		//This thing relies on having a block tick scheduled for the exact tick the output signal would change
		//But the implementation in Level for block ticks only lets you have 1 scheduled per position at a time
		//Most-recently-scheduled block tick takes priority
		//And there's no way to check when the previously-scheduled tick is scheduled for (you can only check for if it's for this current tick)
		//So we sometimes need to override whatever tick was previously scheduled with whatever is currently calculated as correct
		if(!queuedPulses.isEmpty()){
			//We need to schedule the next change in output value
			final long tickCountStandard = standardizedTickCount();
			long nextChange = Integer.MAX_VALUE;
			boolean willChange = false;
			Iterator<Pulse> pulseIterator = queuedPulses.iterator();
			while(pulseIterator.hasNext()){
				Pulse pulse = pulseIterator.next();
				if(pulse.isExpired(tickCountStandard)){
					//Clean up expired pulses
					pulseIterator.remove();
				}else{
					long pulseNext = pulse.tillNextChange(tickCountStandard);
					if(pulseNext > 0){
						willChange = true;
						nextChange = Math.min(pulseNext, nextChange);
					}
				}
			}
			if(willChange){
				if(level.getBlockTicks() instanceof LevelTicks lTicks){
					//Cancel any previously-scheduled tick at this location
					lTicks.clearArea(BoundingBox.fromCorners(worldPosition, worldPosition));
				}
				level.scheduleTick(worldPosition, getOwner(), (int) nextChange, TickPriority.NORMAL);
			}
		}
	}

	public void queuePulse(Pulse newPulse){
		queuedPulses.add(newPulse);
		setChanged();
		updateScheduledTick();
	}

	/**
	 * For any indefinite pulses currently in the queue, convert them to pulses with definite end-times
	 * @param remainingDurationReds The number of redstone-ticks from now that indefinite pulses should terminate
	 * @return Whether any indefinite pulses were changed
	 */
	public boolean limitIndefinitePulses(int remainingDurationReds){
		final long tickCountStandard = standardizedTickCount();
		boolean didChange = false;
		for(int i = 0; i < queuedPulses.size(); i++){
			Pulse p = queuedPulses.get(i);
			if(p.isIndefinite){
				queuedPulses.set(i, p.withEndTime(tickCountStandard, remainingDurationReds));
				didChange = true;
			}
		}
		if(didChange){
			updateScheduledTick();
		}
		return didChange;
	}

	@Override
	public void serverTick(){
		ticksExisted++;
		lastGameTick = level.getGameTime();
		setChanged();
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.saveAdditional(nbt, registries);
		nbt.putLong("existed", ticksExisted);
		nbt.putLong("last_game_tick", lastGameTick);
		for(int i = 0; i < queuedPulses.size(); i++){
			nbt.put("pulse_" + i, Pulse.CODEC.encodeStart(NbtOps.INSTANCE, queuedPulses.get(i)).getOrThrow());
		}
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.loadAdditional(nbt, registries);
		ticksExisted = nbt.getLong("existed");
		queuedPulses.clear();
		int i = 0;
		while(nbt.contains("pulse_" + i)){
			queuedPulses.add(Pulse.CODEC.decode(NbtOps.INSTANCE, nbt.get("pulse_" + i)).getOrThrow().getFirst());
			i++;
		}
	}

	/**
	 * Standardizes ticksExisted value to be independent of tick sequence - ie, gives same value whether TE has incremented ticksExisted this tick yet
	 * @param ticksExisted Non-standardized ticks existed of the TE, in game ticks
	 * @param hasTickedYet Whether this TE has been ticked by the level this game-tick
	 * @return A standardized tick-count value, in game ticks
	 */
	public static long standardizedTickCount(long ticksExisted, boolean hasTickedYet){
		return hasTickedYet ? ticksExisted : ticksExisted + 1;
	}

	public long standardizedTickCount(){
		return standardizedTickCount(ticksExisted, level.getGameTime() == lastGameTick);
	}

	/**
	 * @param startTickStandardized ticksExisted value at which this pulse starts, after converting with standardizedTickCount
	 * @param endTickStandardized ticksExisted value at which this pulse has stopped (exclusive), after converting with standardizedTickCount
	 * @param power Signal power of this pulse while active
	 */
	public static record Pulse(long startTickStandardized, long endTickStandardized, float power, boolean isIndefinite){

		public static final Codec<Pulse> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.LONG.fieldOf("end_std").forGetter(Pulse::endTickStandardized), Codec.LONG.fieldOf("start_std").forGetter(Pulse::startTickStandardized), Codec.FLOAT.fieldOf("power").forGetter(Pulse::power), Codec.BOOL.fieldOf("indefinite").forGetter(Pulse::isIndefinite)).apply(instance, Pulse::new));

		public Pulse{

		}

		/**
		 * @param tickCountStandardized Current tick count, standardized
		 * @param startDelayReds in redstone ticks
		 * @param power Signal power of this pulse while active
		 */
		public static Pulse createIndefinitePulse(long tickCountStandardized, int startDelayReds, float power){
			return new Pulse(tickCountStandardized + (long) startDelayReds * RedstoneUtil.DELAY, 0, power, true);
		}

		/**
		 * @param tickCountStandardized Current tick count, standardized
		 * @param durationReds In redstone ticks
		 * @param startDelayReds in redstone ticks
		 * @param power Signal power of this pulse while active
		 */
		public static Pulse createDefinitePulse(long tickCountStandardized, int durationReds, int startDelayReds, float power){
			return new Pulse(tickCountStandardized + (long) startDelayReds * RedstoneUtil.DELAY, tickCountStandardized + (long) startDelayReds * RedstoneUtil.DELAY + (long) durationReds * RedstoneUtil.DELAY, power, false);
		}

		public Pulse withEndTime(long tickCountStandardized, int remainingDurationReds){
			return new Pulse(startTickStandardized, tickCountStandardized + (long) remainingDurationReds * RedstoneUtil.DELAY, power, false);
		}

		public Pulse withDelay(int delayReds){
			long delay = delayReds * (long) RedstoneUtil.DELAY;
			return new Pulse(startTickStandardized + delay, endTickStandardized + delay, power, isIndefinite);
		}

		public boolean isActive(long tickCountStandardized){
			return tickCountStandardized >= startTickStandardized && (isIndefinite || tickCountStandardized < endTickStandardized);
		}

		public boolean isExpired(long tickCountStandardized){
			return !isIndefinite && (tickCountStandardized >= endTickStandardized || startTickStandardized >= endTickStandardized);
		}

		public long tillNextChange(long tickCountStandardized){
			return tickCountStandardized >= startTickStandardized ? isIndefinite ? -1 : endTickStandardized - tickCountStandardized : startTickStandardized - tickCountStandardized;
		}
	}
}
