package com.Da_Technomancer.essentials.blocks;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

public class BlockUtil{

	/**
	 * For finding which bounding box within a block is targetted by raytracing. Used for block with multiple bounding boxes
	 * @param boxes A list containing bounding boxes. May contain null elements
	 * @param start The starting position vector. Subtracting the block's position from this in advance is suggested
	 * @param end The ending position vector. Subtracting the block's position from this in advance is suggested
	 */
	@Nullable
	public static AxisAlignedBB selectionRaytrace(ArrayList<AxisAlignedBB> boxes, Vec3d start, Vec3d end){
		if(boxes == null || boxes.size() == 0){
			return null;
		}

		float dist = Integer.MAX_VALUE;
		AxisAlignedBB closest = null;

		for(AxisAlignedBB box : boxes){
			if(box == null){
				continue;
			}
			Optional<Vec3d> result = box.rayTrace(start, end);
			if(result.isPresent() && dist > result.get().subtract(start).lengthSquared()){
				dist = (float) result.get().subtract(start).lengthSquared();
				closest = box;
			}
		}

		return closest;
	}
}
