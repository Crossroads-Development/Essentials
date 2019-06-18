package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;

public class HopperFilterRenderer extends TileEntityRenderer<HopperFilterTileEntity>{

	@Override
	public void render(HopperFilterTileEntity te, double x, double y, double z, float partialTicks, int destroyStage){
		if(!te.getWorld().isAreaLoaded(te.getPos(), 0) || te.getFilter().isEmpty()){
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.translated(x + 0.5D, y + 0.5D, z + 0.5D);
		GlStateManager.scalef(0.5F, 0.5F, 0.5F);
		GlStateManager.pushLightingAttributes();
		RenderHelper.enableStandardItemLighting();
		Minecraft.getInstance().getItemRenderer().renderItem(te.getFilter(), ItemCameraTransforms.TransformType.FIXED);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popAttributes();
		GlStateManager.popMatrix();
	}
}
