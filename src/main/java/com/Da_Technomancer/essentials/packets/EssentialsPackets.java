package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class EssentialsPackets{

	public static SimpleChannel channel;
	private static int index = 0;

	public static void preInit(){
		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Essentials.MODID, "channel"), () -> "1.0.0", (s) -> s.equals("1.0.0"), (s) -> s.equals("1.0.0"));

		registerPacket(SendSlotFilterToClient.class);
		registerPacket(ConfigureWrenchOnServer.class);
		registerPacket(SendFloatToClient.class);
		registerPacket(SendFloatToServer.class);
		registerPacket(SendNBTToClient.class);
		registerPacket(SendNBTToServer.class);
	}

	private static <T extends Packet> void registerPacket(Class<T> clazz){
		channel.registerMessage(index++, clazz, PacketManager::encode, (buf) -> PacketManager.decode(buf, clazz), PacketManager::activate);
	}
}
