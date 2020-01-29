package com.Da_Technomancer.essentials.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.sun.javafx.geom.Vec3f;
import net.minecraft.client.renderer.Quaternion;
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
	 * @param light Whether to use light (if false, this glows in the dark)
	 */
	public static void addVertexBlock(IVertexBuilder builder, MatrixStack matrix, float[] pos, float u, float v, float[] normal, boolean light){
		if(light){
			builder.pos(matrix.getLast().getPositionMatrix(), pos[0], pos[1], pos[2]).color(1F, 1F, 1F, 1F).tex(u, v).lightmap(0, 240).normal(normal[0], normal[1], normal[2]).endVertex();
		}else{
			builder.pos(matrix.getLast().getPositionMatrix(), pos[0], pos[1], pos[2]).color(1F, 1F, 1F, 1F).tex(u, v).lightmap(240, 240).normal(normal[0], normal[1], normal[2]).endVertex();
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
	public static float[] findNormal(float[] point0, float[] point1, float[] point2){
		float[] vec0 = new float[3];
		float[] vec1 = new float[3];
		for(int i = 0; i < 3; i++){
			vec0[i] = point1[i] - point0[i];
			vec1[i] = point2[i] - point0[i];
		}
		return new float[] {vec0[1] * vec1[2] - vec0[2] * vec1[1], vec0[2] * vec1[0] - vec0[0] * vec1[2], vec0[0] * vec1[1] - vec0[1] * vec1[0]};
	}

	public static float[] toArray(Vec3f vec){
		return new float[] {vec.x, vec.y, vec.z};
	}

	public static Vec3f toVec(float[] array){
		return new Vec3f(array[0], array[1], array[2]);
	}

	/**
	 * Projects a quaternion onto a vector (finding the "twist" of the quaternion)
	 * Used for rendering quads to face towards the player while remaining in a certain plane
	 *
	 * Based on: https://stackoverflow.com/questions/3684269/component-of-a-quaternion-rotation-around-an-axis
	 * @param quaternion The quaternion to project (usually camera angle)
	 * @param direction The vector to project onto; Does not need to be normalized
	 * @return The twist of quaternion about direction; it will specify a rotation about an axis parallel to direction or a zero quaternion
	 */
	@OnlyIn(Dist.CLIENT)
	public static Quaternion twistQuat(Quaternion quaternion, Vec3f direction){
		direction = new Vec3f(direction);//Copy the vector to prevent writing back to the caller
		direction.normalize();//Normalize direction
		direction.mul(new Vec3f(quaternion.getX(), quaternion.getY(), quaternion.getZ()).dot(direction));//Project the vector components of the quaternion onto the direction
		Quaternion twist = new Quaternion(direction.x, direction.y, direction.z, quaternion.getW());
		twist.normalize();//Normalize the twist; Note that Minecraft's implementation of normalize will zero the quaternion rather than return a singularity
		return twist;
	}
}
