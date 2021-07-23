package com.Da_Technomancer.essentials.packets;

import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class SendFloatToServer extends ServerPacket{

	public SendFloatToServer(){

	}

	public BlockPos pos;
	public byte id;
	public float val;

	public SendFloatToServer(int id, float val, BlockPos pos){
		this((byte) id, val, pos);
	}

	public SendFloatToServer(byte id, float val, BlockPos pos){
		this.id = id;
		this.val = val;
		this.pos = pos;
	}

	private static final Field[] FIELDS = fetchFields(SendFloatToServer.class, "pos", "id", "val");

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(@Nullable ServerPlayer player){
		if(player != null){
			BlockEntity te = player.getCommandSenderLevel().getBlockEntity(pos);

			if(te instanceof IFloatReceiver){
				((IFloatReceiver) te).receiveFloat(id, val, player);
			}
		}
	}
}
