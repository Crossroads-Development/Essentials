package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

public class HopperFilterRenderer extends BlockEntityRenderer<HopperFilterTileEntity>{

	protected HopperFilterRenderer(BlockEntityRenderDispatcher dispatcher){
		super(dispatcher);
	}

	@Override
	public void render(HopperFilterTileEntity te, float v, PoseStack matrix, MultiBufferSource renderBuf, int combinedLight, int combinedOverlay){
		if(te.getFilter().isEmpty()){
			return;
		}

		matrix.pushPose();
		matrix.translate(0.5D, 0.5D, 0.5D);
		matrix.scale(0.5F, 0.5F, 0.5F);
		Minecraft.getInstance().getItemRenderer().renderStatic(te.getFilter(), ItemTransforms.TransformType.FIXED, combinedLight, combinedOverlay, matrix, renderBuf);
		matrix.popPose();
	}
}
