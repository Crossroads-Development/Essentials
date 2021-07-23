package com.Da_Technomancer.essentials.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class SendLongToClient extends ClientPacket{

	public SendLongToClient(){

	}

	public BlockPos pos;
	public byte id;
	public long val;

	public SendLongToClient(int id, long val, BlockPos pos){
		this((byte) id, val, pos);
	}

	public SendLongToClient(byte id, long val, BlockPos pos){
		this.id = id;
		this.val = val;
		this.pos = pos;
	}

	private static final Field[] FIELDS = fetchFields(SendLongToClient.class, "pos", "id", "val");

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(){
		BlockEntity te = Minecraft.getInstance().level.getBlockEntity(pos);

		if(te instanceof ILongReceiver){
			((ILongReceiver) te).receiveLong(id, val, null);
		}
	}
}
