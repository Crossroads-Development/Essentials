package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class HopperFilterRenderer extends TileEntityRenderer<HopperFilterTileEntity>{

	protected HopperFilterRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}

	@Override
	public void render(HopperFilterTileEntity te, float v, MatrixStack matrix, IRenderTypeBuffer renderBuf, int combinedLight, int combinedOverlay){
		if(!te.getWorld().isAreaLoaded(te.getPos(), 0) || te.getFilter().isEmpty()){
			return;
		}

		//TODO test this
		matrix.push();
		matrix.translate(0.5D, 0.5D, 0.5D);
		matrix.scale(0.5F, 0.5F, 0.5F);
		Minecraft.getInstance().getItemRenderer().renderItem(te.getFilter(), ItemCameraTransforms.TransformType.FIXED, combinedLight, combinedOverlay, matrix, renderBuf);
		matrix.pop();
	}
}
