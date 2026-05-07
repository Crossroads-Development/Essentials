package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.packets.INBTReceiver;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.DelayCircuitContainer;
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
import net.minecraft.world.ticks.TickPriority;

import javax.annotation.Nullable;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.delayCircuit;

public class DelayCircuitTileEntity extends AbstractTimeCircuitTileEntity implements MenuProvider, INBTReceiver{

	public static final BlockEntityType<DelayCircuitTileEntity> TYPE = ESTileEntity.createType(DelayCircuitTileEntity::new, delayCircuit);

	private static final int MIN_DELAY = 1;

	public int settingDelay = 2;
	public String settingStrDelay = "2";

	public DelayCircuitTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.delayCircuit;
	}

	@Override
	public void handleInputChange(TickPriority priority){
		float[] inputs = getInputs(getOwner());
		float input = inputs[1];
		if(queuedPulses.isEmpty() ? RedstoneUtil.didChange(input, getOutput()) : RedstoneUtil.didChange(input, queuedPulses.getLast().power())){
			//We need to update the previous 'permanent' pulse to now end when this one starts
			limitIndefinitePulses(settingDelay);
            //Make the new output permanent until further notice
            queuePulse(Pulse.createIndefinitePulse(standardizedTickCount(), settingDelay, input));
        }
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.saveAdditional(nbt, registries);
		nbt.putInt("setting_d", settingDelay);
		nbt.putString("setting_s_d", settingStrDelay);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.loadAdditional(nbt, registries);
		settingDelay = nbt.getInt("setting_d");
		settingStrDelay = nbt.getString("setting_s_d");
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.delay_circuit");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player){
		return new DelayCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), worldPosition, settingStrDelay));
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		int prevSettingDelay = settingDelay;
		settingDelay = Math.max(MIN_DELAY, Math.round(nbt.getFloat("value_0")));
		settingStrDelay = nbt.getString("text_0");
		if(prevSettingDelay != settingDelay){
			queuedPulses.clear();//Just wipe all the pulses in the queue - it's cleaner this way
		}
		setChanged();
		recalculateOutput();
	}
}
