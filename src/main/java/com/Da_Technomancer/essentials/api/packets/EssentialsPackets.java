package com.Da_Technomancer.essentials.api.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class EssentialsPackets{

	public static SimpleChannel channel;
	private static int index = 0;

	public static void preInit(){
		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Essentials.MODID, "channel"), () -> "1.0.0", (s) -> s.equals("1.0.0"), (s) -> s.equals("1.0.0"));

		registerPacket(ConfigureWrenchOnServer.class);
		registerPacket(SendFloatToClient.class);
		registerPacket(SendFloatToServer.class);
		registerPacket(SendNBTToClient.class);
		registerPacket(SendNBTToServer.class);
		registerPacket(SendLongToClient.class);
	}

	private static <T extends Packet> void registerPacket(Class<T> clazz){
		channel.registerMessage(index++, clazz, PacketManager::encode, (buf) -> PacketManager.decode(buf, clazz), PacketManager::activate);
	}
}
