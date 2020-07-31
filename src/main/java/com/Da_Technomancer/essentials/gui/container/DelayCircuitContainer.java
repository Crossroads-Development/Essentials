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
public class DelayCircuitContainer extends Container{

	public int delay;
	public String delayStr;
	public BlockPos pos;

	@ObjectHolder("delay_circuit")
	private static ContainerType<DelayCircuitContainer> TYPE = null;

	public DelayCircuitContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		this(id, playerInventory, data == null ? 0 : data.readInt(), data == null ? null : data.readString(), data == null ? null : data.readBlockPos());
	}

	public DelayCircuitContainer(int id, PlayerInventory playerInventory, int delay, String settingStrDelay, BlockPos pos){
		super(TYPE, id);
		this.delay = delay;
		this.delayStr = settingStrDelay;
		this.pos = pos;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn){
		return true;
	}
}
