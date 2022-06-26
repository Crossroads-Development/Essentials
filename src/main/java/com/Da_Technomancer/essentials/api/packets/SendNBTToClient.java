package com.Da_Technomancer.essentials.api.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class SendNBTToClient extends ClientPacket{

	public SendNBTToClient(){

	}

	public BlockPos pos;
	public CompoundTag nbt;

	public SendNBTToClient(CompoundTag nbt, BlockPos pos){
		this.nbt = nbt;
		this.pos = pos;
	}

	private static final Field[] FIELDS = fetchFields(SendNBTToClient.class, "pos", "nbt");

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(){
		BlockEntity te = Minecraft.getInstance().level.getBlockEntity(pos);

		if(te instanceof INBTReceiver){
			((INBTReceiver) te).receiveNBT(nbt, null);
		}
	}
}
