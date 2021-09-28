package com.Da_Technomancer.essentials.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class SendNBTToServer extends ServerPacket{

	public SendNBTToServer(){

	}

	public CompoundTag nbt;
	public BlockPos pos;

	public SendNBTToServer(CompoundTag nbt, BlockPos pos){
		this.nbt = nbt;
		this.pos = pos;
	}

	private static final Field[] FIELDS = fetchFields(SendNBTToServer.class, "nbt", "pos");

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(@Nullable ServerPlayer player){
		if(player != null){
			BlockEntity te = player.getCommandSenderWorld().getBlockEntity(pos);

			if(te instanceof INBTReceiver){
				((INBTReceiver) te).receiveNBT(nbt, player);
			}
		}
	}
}
