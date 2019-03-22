package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class TESRRegistry{

	public static void init(){
		ClientRegistry.bindTileEntitySpecialRenderer(HopperFilterTileEntity.class, new HopperFilterRenderer());
	}
}
