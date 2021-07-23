package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.lang.reflect.Field;

/**
 * A (non-mandatory) subclass of Packet for sending Packets from the server to the client
 */
public abstract class ClientPacket extends Packet{

	protected static Field[] fetchFields(Class<? extends ClientPacket> clazz, String... fieldNames){
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
		if(context.getDirection() != NetworkDirection.PLAY_TO_CLIENT){
			Essentials.logger.error("Packet " + toString() + " received on wrong side:" + context.getDirection());
			return;
		}

		Minecraft.getInstance().tell(this::run);
	}

	/**
	 * Perform the operation of this packet, without needing to enqueue the task or check direction
	 */
	protected abstract void run();
}
