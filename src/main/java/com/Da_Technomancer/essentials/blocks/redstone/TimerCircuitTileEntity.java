package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.packets.INBTReceiver;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.TimerCircuitContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.timerCircuit;

public class TimerCircuitTileEntity extends AbstractTimeCircuitTileEntity implements MenuProvider, INBTReceiver{

	public static final BlockEntityType<TimerCircuitTileEntity> TYPE = ESTileEntity.createType(TimerCircuitTileEntity::new, timerCircuit);

	private static final int MIN_PERIOD = 1;
	private static final int MIN_DURATION = 0;

	public int settingPeriod = 4;
	public String settingStrPeriod = "4";
	public int settingDuration = 2;
	public String settingStrDuration = "2";

	public TimerCircuitTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.timerCircuit;
	}

	@Override
	public void serverTick(){
		super.serverTick();

		//Occasionally validate the pulse queue and purge anything that shouldn't be in it
		if((settingPeriod <= 0 || (ticksExisted / RedstoneUtil.DELAY % settingPeriod) == 0) && !queuedPulses.isEmpty()){
			Iterator<Pulse> pulseIterator = queuedPulses.iterator();
			final long standardTickCount = standardizedTickCount();
			while(pulseIterator.hasNext()){
				Pulse pulse = pulseIterator.next();
				if(pulse.isExpired(standardTickCount)){
					pulseIterator.remove();
				}else if(pulse.startTickStandardized() - standardTickCount > 3L * RedstoneUtil.DELAY * settingPeriod){
					//Shouldn't be anything this delayed in the queue
					pulseIterator.remove();
				}else if(pulse.endTickStandardized() - pulse.startTickStandardized() != (long) RedstoneUtil.DELAY * settingDuration){
					//Duration is wrong
					pulseIterator.remove();
				}
			}
		}

		//Keep a next pulse queued up
		if(queuedPulses.size() < 2 && settingDuration > 0){
			if(queuedPulses.isEmpty()){
				queuePulse(Pulse.createDefinitePulse(standardizedTickCount(), settingDuration, 1, 1F));
			}
			Pulse lastPulse = queuedPulses.getLast();
			queuePulse(lastPulse.withDelay(settingPeriod));
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.saveAdditional(nbt, registries);
		nbt.putInt("setting_p", settingPeriod);
		nbt.putString("setting_s_p", settingStrPeriod);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries){
		CompoundTag nbt = super.getUpdateTag(registries);
		nbt.putInt("setting_p", settingPeriod);
		nbt.putString("setting_s_p", settingStrPeriod);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
		return nbt;
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.loadAdditional(nbt, registries);
		settingPeriod = nbt.getInt("setting_p");
		settingStrPeriod = nbt.getString("setting_s_p");
		settingDuration = nbt.getInt("setting_d");
		settingStrDuration = nbt.getString("setting_s_d");
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.timer_circuit");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player){
		return new TimerCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), worldPosition, settingStrPeriod, settingStrDuration));
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		int prevPeriod = settingPeriod;
		int prevDuration = settingDuration;
		settingPeriod = Math.max(MIN_PERIOD, Math.round(nbt.getFloat("value_0")));
		settingStrPeriod = nbt.getString("text_0");
		settingDuration = Math.max(MIN_DURATION, Math.round(nbt.getFloat("value_1")));
		settingStrDuration = nbt.getString("text_1");
		if(prevPeriod != settingPeriod || prevDuration != settingDuration){
			//Reset the pulses
			queuedPulses.clear();
			recalculateOutput();
		}
		setChanged();
	}
}
