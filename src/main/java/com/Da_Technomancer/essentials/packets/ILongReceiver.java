package com.Da_Technomancer.essentials.packets;

import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public interface ILongReceiver{

	public void receiveLong(byte id, long value, @Nullable ServerPlayerEntity sender);
}
