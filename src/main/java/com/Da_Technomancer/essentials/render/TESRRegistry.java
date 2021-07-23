package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.RedstoneTransmitterTileEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class TESRRegistry{

	public static void init(){
		BlockEntityRenderers.register(HopperFilterTileEntity.TYPE, HopperFilterRenderer::new);
		BlockEntityRenderers.register(RedstoneTransmitterTileEntity.TYPE, LinkLineRenderer::new);
	}
}
