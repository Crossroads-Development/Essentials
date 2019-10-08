package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import com.Da_Technomancer.essentials.tileentities.RedstoneTransmitterTileEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TESRRegistry{

	public static void init(){
		ClientRegistry.bindTileEntitySpecialRenderer(HopperFilterTileEntity.class, new HopperFilterRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(RedstoneTransmitterTileEntity.class, new LinkLineRenderer<>());
	}
}
