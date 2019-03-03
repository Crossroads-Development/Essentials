package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;

public class EssentialsProperties{

	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	//0: Empty, 1: water, 2: lava, 3: coal, 4: glowstone, 5: reserved, 6: soul sand, 7: poison potato
	public static final PropertyInteger BRAZIER_CONTENTS = PropertyInteger.create("br_cont", 0, 7);
	public static final PropertyBool REDSTONE_BOOL = PropertyBool.create("redstone_bool");
	public static final PropertyBool EXTENDED = PropertyBool.create("extended");
	//0: No head, 1: Head in positive direction, 2: Head in negative direction
	public static final PropertyInteger HEAD = PropertyInteger.create("head", 0, 2);
	public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);
}
