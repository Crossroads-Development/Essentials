package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class HopperFilterRenderer extends TileEntitySpecialRenderer<HopperFilterTileEntity>{

	@Override
	public void render(HopperFilterTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
		if(!te.getWorld().isBlockLoaded(te.getPos(), false) || te.getFilter().isEmpty()){
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y + 0.5D, z + 0.5D);
		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		GlStateManager.pushLightingAttrib();
		RenderHelper.enableStandardItemLighting();
		Minecraft.getInstance().getRenderItem().renderItem(te.getFilter(), ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}
}
