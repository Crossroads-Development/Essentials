package com.Da_Technomancer.essentials.packets;

import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public interface INBTReceiver{

	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayer sender);
}
