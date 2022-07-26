package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.blocks.HopperFilterTileEntity;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneTransmitterTileEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class TESRRegistry{

	public static void init(EntityRenderersEvent.RegisterRenderers e){
		registerTESR(e, HopperFilterTileEntity.TYPE, HopperFilterRenderer::new);
		registerTESR(e, RedstoneTransmitterTileEntity.TYPE, LinkLineRenderer::new);
	}

	private static <T extends BlockEntity> void registerTESR(EntityRenderersEvent.RegisterRenderers e, BlockEntityType<? extends T> teType, BlockEntityRendererProvider<T> tesrConstructor){
		e.registerBlockEntityRenderer(teType, tesrConstructor);
	}
}
