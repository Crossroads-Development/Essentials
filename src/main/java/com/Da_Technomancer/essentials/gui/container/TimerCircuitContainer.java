package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class TimerCircuitContainer extends CircuitContainer{

	@ObjectHolder("timer_circuit")
	private static ContainerType<TimerCircuitContainer> TYPE = null;

	public TimerCircuitContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		super(TYPE, id, playerInventory, data);
	}

	@Override
	public int inputBars(){
		return 2;
	}
}
