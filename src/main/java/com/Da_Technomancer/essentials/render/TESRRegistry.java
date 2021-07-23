package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterBlockEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.RedstoneTransmitterBlockEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TESRRegistry{

	public static void init(){
		ClientRegistry.bindBlockEntityRenderer(HopperFilterBlockEntity.TYPE, HopperFilterRenderer::new);
		ClientRegistry.bindBlockEntityRenderer(RedstoneTransmitterBlockEntity.TYPE, LinkLineRenderer::new);
	}
}
