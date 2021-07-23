package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterBlockEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.BlockEntityRenderer;
import net.minecraft.client.renderer.tileentity.BlockEntityRendererDispatcher;

public class HopperFilterRenderer extends BlockEntityRenderer<HopperFilterBlockEntity>{

	protected HopperFilterRenderer(BlockEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}

	@Override
	public void render(HopperFilterBlockEntity te, float v, MatrixStack matrix, IRenderTypeBuffer renderBuf, int combinedLight, int combinedOverlay){
		if(te.getFilter().isEmpty()){
			return;
		}

		matrix.pushPose();
		matrix.translate(0.5D, 0.5D, 0.5D);
		matrix.scale(0.5F, 0.5F, 0.5F);
		Minecraft.getInstance().getItemRenderer().renderStatic(te.getFilter(), ItemCameraTransforms.TransformType.FIXED, combinedLight, combinedOverlay, matrix, renderBuf);
		matrix.popPose();
	}
}
