package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class LinkLineRenderer<T extends TileEntity & ILinkTE> extends TileEntityRenderer<T>{

	private static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/model/link_line.png");

	@Override
	public void render(T te, double x, double y, double z, float partialTicks, int destroyStage){
		if(te == null || !te.getWorld().isBlockLoaded(te.getPos()) || !ILinkTE.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.MAIN_HAND)) && !ILinkTE.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.OFF_HAND))){
			return;
		}

		GlStateManager.pushMatrix();
		GlStateManager.pushLightingAttributes();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1, 1, 1, 0.7F);
		float brightX = GLX.lastBrightnessX;
		float brightY = GLX.lastBrightnessY;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240, 240);
		Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);

		x += 0.5D;
		y += 0.5D;
		z += 0.5D;

		GlStateManager.translated(x, y, z);

		y -= Minecraft.getInstance().player.getEyeHeight();

		BufferBuilder vb = Tessellator.getInstance().getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for(BlockPos link : te.getLinks()){
			Vec3d offsetVec = new Vec3d(x, y, z);
			Vec3d linkVec = new Vec3d(link.getX(), link.getY(), link.getZ());
			double length = linkVec.length();
			//The width vector is the vector from the player's eyes to the closest point on the link line (were it extended indefinitely) to the player's eyes, all cross the link line vector.
			//If you want to know where this formula comes from... I'm not cramming a quarter page of vector geometry into these comments. Google it like a functioning adult
			Vec3d widthVec = offsetVec.add(linkVec.scale(-linkVec.dotProduct(offsetVec) / length / length)).crossProduct(linkVec);
			widthVec = widthVec.scale(0.15D / widthVec.length());
			vb.pos(link.getX() - widthVec.x, link.getY() - widthVec.y, link.getZ() - widthVec.z).tex(0, length).endVertex();
			vb.pos(link.getX() + widthVec.x, link.getY() + widthVec.y, link.getZ() + widthVec.z).tex(1, length).endVertex();
			vb.pos(widthVec.x, widthVec.y, widthVec.z).tex(1, 0).endVertex();
			vb.pos(-widthVec.x, -widthVec.y, -widthVec.z).tex(0, 0).endVertex();
		}
		Tessellator.getInstance().draw();
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, brightX, brightY);
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popAttributes();
		GlStateManager.popMatrix();
	}
}
