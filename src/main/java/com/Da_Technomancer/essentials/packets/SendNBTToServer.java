package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class SendNBTToServer extends ServerPacket{

	public SendNBTToServer(){

	}

	public CompoundNBT nbt;
	public BlockPos pos;

	public SendNBTToServer(CompoundNBT nbt, BlockPos pos){
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
			BlockEntity te = player.getCommandSenderLevel().getBlockEntity(pos);

			if(te instanceof INBTReceiver){
				((INBTReceiver) te).receiveNBT(nbt, player);
			}
		}
	}
}
