package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class EssentialsPackets{

	public static SimpleNetworkWrapper network;

	public static void preInit(){
		network = NetworkRegistry.INSTANCE.newSimpleChannel(Essentials.MODID + ".chan");

		int packetId = 5;
		network.registerMessage(SendSlotFilterToClient.class, SendSlotFilterToClient.class, packetId++, Side.CLIENT);
	}
}
