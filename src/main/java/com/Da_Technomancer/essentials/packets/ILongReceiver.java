package com.Da_Technomancer.essentials.packets;

import net.minecraft.entity.player.ServerPlayer;

import javax.annotation.Nullable;

public interface ILongReceiver{

	public void receiveLong(byte id, long value, @Nullable ServerPlayer sender);
}
