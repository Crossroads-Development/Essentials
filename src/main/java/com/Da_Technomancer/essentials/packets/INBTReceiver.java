package com.Da_Technomancer.essentials.packets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

public interface INBTReceiver{

	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender);
}
