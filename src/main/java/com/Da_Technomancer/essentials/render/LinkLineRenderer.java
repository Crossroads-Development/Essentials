package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class LinkLineRenderer<T extends TileEntity & ILinkTE> extends TileEntityRenderer<T>{

	public static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "model/link_line");

	public LinkLineRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}

	@Override
	public void render(T te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
		if(!te.getWorld().isBlockLoaded(te.getPos()) || !ILinkTE.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.MAIN_HAND)) && !ILinkTE.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.OFF_HAND))){
			return;
		}

		//TODO test

		matrix.push();
		RenderSystem.pushLightingAttributes();
		RenderSystem.enableBlend();
		RenderSystem.disableLighting();
		RenderSystem.disableCull();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1, 1, 1, 0.7F);
		//TODO disable lighting fully

		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(TEXTURE);
		IVertexBuilder builder = buffer.getBuffer(RenderType.translucentNoCrumbling());

		Quaternion camera = Minecraft.getInstance().getRenderManager().getCameraOrientation();
		final float width = 0.15F;

		for(BlockPos link : te.getLinks()){
			Vec3f line = new Vec3f(link.getX(), link.getY(), link.getZ());
			//The camera rotation expects the quad to be aligned in the x-y plane beforehand
			//We therefore create a "width" vector perpendicular to the path in the x-y plane
			Vec3f widthVec = new Vec3f(+line.y, -line.x, 0);//Cross product of line with z-axis
			if(line.x == 0 && line.y == 0 && line.z != 0){
				//Special case: the line is parallel to the z-axis.
				widthVec = new Vec3f(1, 0, 0);//TODO test; this value was not derived- it was guessed
			}
			widthVec.normalize();
			widthVec.mul(width);
			matrix.push();
			matrix.rotate(RenderUtil.twistQuat(camera, line));
			float[] startL = new float[] {-widthVec.x, -widthVec.y, -widthVec.z};
			float[] startR = RenderUtil.toArray(widthVec);
			float[] endL = new float[] {line.x - widthVec.x, line.y - widthVec.y, line.z - widthVec.z};
			float[] endR = new float[] {line.x + widthVec.x, line.y + widthVec.y, line.z + widthVec.z};
			float[] normal = RenderUtil.findNormal(startL, startR, endL);//In theory, this should be the z-axis

			RenderUtil.addVertexBlock(builder, matrix, startL, sprite.getMinU(), sprite.getMinV(), normal, false);
			RenderUtil.addVertexBlock(builder, matrix, startR, sprite.getMaxU(), sprite.getMinV(), normal, false);
			RenderUtil.addVertexBlock(builder, matrix, endR, sprite.getMaxU(), sprite.getMaxV(), normal, false);
			RenderUtil.addVertexBlock(builder, matrix, endL, sprite.getMinU(), sprite.getMaxV(), normal, false);
			matrix.pop();
		}

		RenderSystem.color4f(1, 1, 1, 1);
		RenderSystem.enableCull();
		RenderSystem.enableLighting();
		RenderSystem.disableBlend();
		RenderSystem.popAttributes();
		matrix.pop();



//		//TODO
//
//		GlStateManager.pushMatrix();
//		GlStateManager.pushLightingAttributes();
//		GlStateManager.enableBlend();
//		GlStateManager.disableLighting();
//		GlStateManager.disableCull();
//		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//		GlStateManager.color4f(1, 1, 1, 0.7F);
//		float brightX = GLX.lastBrightnessX;
//		float brightY = GLX.lastBrightnessY;
//		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240, 240);
//		Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE);
//
//		x += 0.5D;
//		y += 0.5D;
//		z += 0.5D;
//
//		GlStateManager.translated(x, y, z);
//
//		y -= Minecraft.getInstance().player.getEyeHeight();
//
//		BufferBuilder vb = Tessellator.getInstance().getBuffer();
//		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//		for(BlockPos link : te.getLinks()){
//			Vec3d offsetVec = new Vec3d(x, y, z);
//			Vec3d linkVec = new Vec3d(link.getX(), link.getY(), link.getZ());
//			double length = linkVec.length();
//			//The width vector is the vector from the player's eyes to the closest point on the link line (were it extended indefinitely) to the player's eyes, all cross the link line vector.
//			//If you want to know where this formula comes from... I'm not cramming a quarter page of vector geometry into these comments. Google it like a functioning adult
//			Vec3d widthVec = offsetVec.add(linkVec.scale(-linkVec.dotProduct(offsetVec) / length / length)).crossProduct(linkVec);
//			widthVec = widthVec.scale(0.15D / widthVec.length());
//			vb.pos(link.getX() - widthVec.x, link.getY() - widthVec.y, link.getZ() - widthVec.z).tex(0, length).endVertex();
//			vb.pos(link.getX() + widthVec.x, link.getY() + widthVec.y, link.getZ() + widthVec.z).tex(1, length).endVertex();
//			vb.pos(widthVec.x, widthVec.y, widthVec.z).tex(1, 0).endVertex();
//			vb.pos(-widthVec.x, -widthVec.y, -widthVec.z).tex(0, 0).endVertex();
//		}
//		Tessellator.getInstance().draw();
//		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, brightX, brightY);
//		GlStateManager.color4f(1, 1, 1, 1);
//		GlStateManager.enableCull();
//		GlStateManager.enableLighting();
//		GlStateManager.disableBlend();
//		GlStateManager.popAttributes();
//		GlStateManager.popMatrix();
	}
}
