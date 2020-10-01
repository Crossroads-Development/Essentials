package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class PulseCircuitContainer extends CircuitContainer{

	@ObjectHolder("pulse_circuit")
	private static ContainerType<PulseCircuitContainer> TYPE = null;

	public PulseCircuitContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		super(TYPE, id, playerInventory, data);
	}

	@Override
	public int inputBars(){
		return 1;
	}
}
