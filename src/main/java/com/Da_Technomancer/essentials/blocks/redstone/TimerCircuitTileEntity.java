package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.TimerCircuitContainer;
import com.Da_Technomancer.essentials.api.packets.INBTReceiver;
import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

public class TimerCircuitTileEntity extends CircuitTileEntity implements MenuProvider, INBTReceiver, ITickableTileEntity{

	@ObjectHolder(registryName="block_entity_type", value=Essentials.MODID + ":timer_circuit")
	public static BlockEntityType<TimerCircuitTileEntity> TYPE = null;

	private static final int MIN_PERIOD = 1;
	private static final int MIN_DURATION = 0;

	public int settingPeriod = 4;
	public String settingStrPeriod = "4";
	public int settingDuration = 2;
	public String settingStrDuration = "2";

	private long ticksExisted = 0;

	public TimerCircuitTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.timerCircuit;
	}

	public int timerOutput(){
		//Divide by RedstoneUtil.DELAY to convert from gameticks to redstone ticks
		if((ticksExisted / RedstoneUtil.DELAY) % settingPeriod < settingDuration){
			return 1;
		}else{
			return 0;
		}
	}

	@Override
	public void tick(){
		ticksExisted++;

		int clockTime = (int) (ticksExisted / RedstoneUtil.DELAY) % settingPeriod;
		if(!level.isClientSide && ticksExisted % RedstoneUtil.DELAY == 0 && (clockTime == 0 || clockTime == settingDuration)){
			//Force circuits to recalculate when output changes
			recalculateOutput();
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		nbt.putInt("setting_p", settingPeriod);
		nbt.putString("setting_s_p", settingStrPeriod);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
		nbt.putLong("existed", ticksExisted);
	}

	@Override
	public CompoundTag getUpdateTag(){
		CompoundTag nbt = super.getUpdateTag();
		nbt.putInt("setting_p", settingPeriod);
		nbt.putString("setting_s_p", settingStrPeriod);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
		nbt.putLong("existed", ticksExisted);
		return nbt;
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		settingPeriod = nbt.getInt("setting_p");
		settingStrPeriod = nbt.getString("setting_s_p");
		settingDuration = nbt.getInt("setting_d");
		settingStrDuration = nbt.getString("setting_s_d");
		ticksExisted = nbt.getLong("existed");
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
		settingPeriod = Math.max(MIN_PERIOD, Math.round(nbt.getFloat("value_0")));
		settingStrPeriod = nbt.getString("text_0");
		settingDuration = Math.max(MIN_DURATION, Math.round(nbt.getFloat("value_1")));
		settingStrDuration = nbt.getString("text_1");
		setChanged();
		recalculateOutput();
	}
}
