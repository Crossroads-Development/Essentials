package com.Da_Technomancer.essentials.api.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class SendFloatToClient extends ClientPacket{

	public SendFloatToClient(){

	}

	public BlockPos pos;
	public byte id;
	public float val;

	public SendFloatToClient(int id, float val, BlockPos pos){
		this((byte) id, val, pos);
	}

	public SendFloatToClient(byte id, float val, BlockPos pos){
		this.id = id;
		this.val = val;
		this.pos = pos;
	}

	private static final Field[] FIELDS = fetchFields(SendFloatToClient.class, "pos", "id", "val");

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(){
		if(Minecraft.getInstance().level == null){
			return;
		}
		BlockEntity te = Minecraft.getInstance().level.getBlockEntity(pos);

		if(te instanceof IFloatReceiver){
			((IFloatReceiver) te).receiveFloat(id, val, null);
		}
	}
}
