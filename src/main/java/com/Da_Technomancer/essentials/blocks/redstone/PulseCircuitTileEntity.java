package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.packets.INBTReceiver;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.PulseCircuitContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

import javax.annotation.Nullable;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.*;

public class PulseCircuitTileEntity extends AbstractTimeCircuitTileEntity implements MenuProvider, INBTReceiver{

	public static final BlockEntityType<PulseCircuitTileEntity> TYPE = ESTileEntity.createType(PulseCircuitTileEntity::new, pulseCircuitRising, pulseCircuitFalling, pulseCircuitDual);

	private static final int MIN_DURATION = 1;

	public int settingDuration = 1;
	public String settingStrDuration = "1";

	private float lastInput = 0;

	public PulseCircuitTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	private PulseCircuit.Edge getEdge(){
		Block b = getBlockState().getBlock();
		if(b instanceof PulseCircuit pulseCircuit){
			return pulseCircuit.edge;
		}
		setRemoved();
		return PulseCircuit.Edge.RISING;
	}

	@Override
	public void handleInputChange(TickPriority priority){
		float[] inputs = getInputs(getOwner());
		float input = inputs[1];

		if(lastInput != input){
			boolean activeInput = input > 0;
			boolean wasActive = lastInput > 0;
			if(activeInput != wasActive){
				if(activeInput ? getEdge().start : getEdge().end){
					queuePulse(Pulse.createDefinitePulse(standardizedTickCount(), settingDuration, 1, Math.max(lastInput, input)));
				}
			}
			lastInput = input;
			setChanged();
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.saveAdditional(nbt, registries);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
		nbt.putFloat("last_input", lastInput);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries){
		super.loadAdditional(nbt, registries);
		settingDuration = nbt.getInt("setting_d");
		settingStrDuration = nbt.getString("setting_s_d");
		lastInput = nbt.getBoolean("input") ? 1 : nbt.getFloat("last_input");
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.pulse_circuit_" + getEdge().name);
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player){
		return new PulseCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), worldPosition, settingStrDuration));
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		settingDuration = Math.max(Math.round(nbt.getFloat("value_0")), MIN_DURATION);
		settingStrDuration = nbt.getString("text_0");
		setChanged();
		recalculateOutput();
	}
}
