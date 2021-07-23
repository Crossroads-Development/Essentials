package com.Da_Technomancer.essentials.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class SendNBTToClient extends ClientPacket{

	public SendNBTToClient(){

	}

	public BlockPos pos;
	public CompoundNBT nbt;

	public SendNBTToClient(CompoundNBT nbt, BlockPos pos){
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
