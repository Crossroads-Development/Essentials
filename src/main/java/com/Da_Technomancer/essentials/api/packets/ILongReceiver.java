package com.Da_Technomancer.essentials.api.packets;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface ILongReceiver{

	public void receiveLong(byte id, long value, @Nullable ServerPlayer sender);
}
