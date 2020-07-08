package com.Da_Technomancer.essentials.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.dispenser.IPosition;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RenderUtil{

	public static final int BRIGHT_LIGHT = 240 | (240 << 16);//Lightmap combined coordinate for fixed full brightness

	/**
	 * Adds a vertex to the builder using the BLOCK vertex format
	 * @param builder The active builder
	 * @param matrix The reference matrix
	 * @param x The x position of this vertex
	 * @param y The y position of this vertex
	 * @param z The z position of this vertex
	 * @param u The u coord of this vertex texture mapping
	 * @param v The v coord of this vertex texture mapping
	 * @param normalX The normal x component to this vertex
	 * @param normalY The normal y component to this vertex
	 * @param normalZ The normal z component to this vertex
	 * @param light The light value
	 */
	@OnlyIn(Dist.CLIENT)
	public static void addVertexBlock(IVertexBuilder builder, MatrixStack matrix, float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ, int light){
		builder.pos(matrix.getLast().getMatrix(), x, y, z).color(1F, 1F, 1F, 1F).tex(u, v).lightmap(light).normal(matrix.getLast().getNormal(), normalX, normalY, normalZ).endVertex();
	}

	/**
	 * Adds a vertex to the builder using the BLOCK vertex format
	 * @param builder The active builder
	 * @param matrix The reference matrix
	 * @param pos The position of this vertex
	 * @param u The u coord of this vertex texture mapping
	 * @param v The v coord of this vertex texture mapping
	 * @param normal The normal vector to this vertex
	 * @param alpha The alpha value [0-1]
	 * @param light The combined light coordinate
	 */
	@OnlyIn(Dist.CLIENT)
	public static void addVertexBlock(IVertexBuilder builder, MatrixStack matrix, IPosition pos, float u, float v, IPosition normal, float alpha, int light){
		builder.pos(matrix.getLast().getMatrix(), (float) pos.getX(), (float) pos.getY(), (float) pos.getZ()).color(1F, 1F, 1F, alpha).tex(u, v).lightmap(light).normal(matrix.getLast().getNormal(), (float) normal.getX(), (float) normal.getY(), (float) normal.getZ()).endVertex();
	}

	/**
	 * Finds the width vector for drawing a 2d plane along a set ray
	 *
	 * What is this for?:
	 * When doing rendering, often it is more convenient to draw a single 2d texture and orient it towards the player
	 * Normally when doing this, simple rotate to the camera angle. However, if the 2d texture must be centered on a certain line (like an arrow), the formula is more complex
	 * In that case, use this method to get a width vector to give a quad defined along a line viewable width
	 *
	 * @param rayStPos The start position of the quad (world relative)
	 * @param ray The direction of the fixed line
	 * @param width The total width of the quad to be drawn
	 * @return A vector of magnitude width/2 orthogonal to ray oriented to maximize viewable area by the player
	 */
	@OnlyIn(Dist.CLIENT)
	public static Vector3d findRayWidth(Vector3d rayStPos, Vector3d ray, float width){
		Vector3d cameraPos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		Vector3d relStPos = rayStPos.subtract(cameraPos);//Where the ray starts, relative to the camera
		ray = ray.normalize();
		Vector3d widthVec = relStPos.subtract(ray.scale(ray.dotProduct(relStPos)));//Vector from the camera to the closest point on the ray (were it extended infinitely)
		widthVec = widthVec.crossProduct(ray);//Cross the ray direction with the vector from camera to ray, resulting in a vector orthogonal to the field of vision and the ray
		return widthVec.scale(width / 2 / widthVec.length());
	}
}
