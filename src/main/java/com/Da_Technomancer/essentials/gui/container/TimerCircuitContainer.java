package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class TimerCircuitContainer extends Container{

	public int period;
	public String periodStr;
	public int duration;
	public String durationStr;
	public BlockPos pos;

	@ObjectHolder("timer_circuit")
	private static ContainerType<TimerCircuitContainer> TYPE = null;

	public TimerCircuitContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		this(id, playerInventory, data == null ? 0 : data.readInt(), data == null ? null : data.readString(), data == null ? 0 : data.readInt(), data == null ? null : data.readString(), data == null ? null : data.readBlockPos());
	}

	public TimerCircuitContainer(int id, PlayerInventory playerInventory, int period, String settingStrPeriod, int duration, String settingStrDuration, BlockPos pos){
		super(TYPE, id);
		this.period = period;
		this.periodStr = settingStrPeriod;
		this.duration = duration;
		this.durationStr = settingStrDuration;
		this.pos = pos;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn){
		return true;
	}
}
