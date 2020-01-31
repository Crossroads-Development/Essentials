package com.Da_Technomancer.essentials.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RenderUtil{

	/**
	 * Adds a vertex to the builder using the BLOCK vertex format
	 * @param builder The active builder
	 * @param matrix The reference matrix
	 * @param pos The position of this vertex
	 * @param u The u coord of this vertex texture mapping
	 * @param v The v coord of this vertex texture mapping
	 * @param normal The normal vector to this vertex
	 */
	@OnlyIn(Dist.CLIENT)
	public static void addVertexBlock(IVertexBuilder builder, MatrixStack matrix, Vec3d pos, float u, float v, Vec3d normal){
		addVertexBlock(builder, matrix, pos, u, v, normal, 1, true);
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
	 * @param light Whether to use light (if false, this glows in the dark)
	 */
	@OnlyIn(Dist.CLIENT)
	public static void addVertexBlock(IVertexBuilder builder, MatrixStack matrix, Vec3d pos, float u, float v, Vec3d normal, float alpha, boolean light){
		if(light){
			builder.pos(matrix.getLast().getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z).color(1F, 1F, 1F, alpha).tex(u, v).lightmap(0, 240).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
		}else{
			builder.pos(matrix.getLast().getPositionMatrix(), (float) pos.x, (float) pos.y, (float) pos.z).color(1F, 1F, 1F, alpha).tex(u, v).lightmap(240, 240).normal((float) normal.x, (float) normal.y, (float) normal.z).endVertex();
		}
	}

	/**
	 * Finds the normal vector for a vertex
	 * If all vertices in a polygon are in the same plane, this result will apply to all
	 * @param point0 The position of the vertex this normal is for
	 * @param point1 The position of an adjacent vertex
	 * @param point2 The position of the other adjacent vertex
	 * @return The normal vector
	 */
	public static Vec3d findNormal(Vec3d point0, Vec3d point1, Vec3d point2){
		point1 = point1.subtract(point0);
		point2 = point2.subtract(point0);
		return point1.crossProduct(point2);
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
	public static Vec3d findRayWidth(Vec3d rayStPos, Vec3d ray, float width){
		Vec3d cameraPos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
		Vec3d relStPos = rayStPos.subtract(cameraPos);//Where the ray starts, relative to the camera
		ray = ray.normalize();
		Vec3d widthVec = relStPos.subtract(ray.scale(ray.dotProduct(relStPos)));//Vector from the camera to the closest point on the ray (were it extended infinitely)
		widthVec = widthVec.crossProduct(ray);//Cross the ray direction with the vector from camera to ray, resulting in a vector orthogonal to the field of vision and the ray
		return widthVec.scale(width / 2 / widthVec.length());
	}
}
