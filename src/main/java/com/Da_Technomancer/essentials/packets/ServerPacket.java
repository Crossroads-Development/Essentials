package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * A (non-mandatory) subclass of Packet for sending Packets from the client to the server
 */
public abstract class ServerPacket extends Packet{

	protected static Field[] fetchFields(Class<? extends ServerPacket> clazz, String... fieldNames){
		Field[] fields = new Field[fieldNames.length];
		try{
			for(int i = 0; i < fieldNames.length; i++){
				fields[i] = clazz.getDeclaredField(fieldNames[i]);
			}
		}catch(NoSuchFieldException e){
			Essentials.logger.error("Failure to specify packet: " + clazz.toString() + "; Report to mod author", e);
			return new Field[0];
		}
		return fields;
	}

	@Override
	protected void consume(NetworkEvent.Context context){
		if(context.getDirection() != NetworkDirection.PLAY_TO_SERVER){
			Essentials.logger.error("Packet " + toString() + " received on wrong side:" + context.getDirection());
			return;
		}

		context.enqueueWork(() -> run(context.getSender()));
	}

	/**
	 * Perform the operation of this packet, without needing to enqueue the task or check direction
	 * @param player The player who sent the packet, or null if n/a
	 */

	protected abstract void run(@Nullable ServerPlayer player);
}
