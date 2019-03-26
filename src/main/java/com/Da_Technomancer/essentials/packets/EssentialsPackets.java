package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class EssentialsPackets{

	public static SimpleChannel channel;

	public static void preInit(){
		channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Essentials.MODID, "channel"), () -> "1.0.0", (s) -> s.equals("1.0.0"), (s) -> s.equals("1.0.0"));

		channel.registerMessage(0, SendSlotFilterToClient.class, PacketManager::encode, (buf) -> PacketManager.decode(buf, SendSlotFilterToClient.class), PacketManager::activate);
	}
}
