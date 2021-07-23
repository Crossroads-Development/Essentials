package com.Da_Technomancer.essentials.packets;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface IFloatReceiver{

	public void receiveFloat(byte id, float value, @Nullable ServerPlayer sender);
}
