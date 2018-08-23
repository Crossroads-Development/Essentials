package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;

public class EssentialsProperties{

	/**0 = wheat, 1 = potato, 2 = carrots, 3 = beetroot, 4 = oak, 5 = birch, 6 = spruce, 7 = jungle, 8 = acacia, 9 = dark oak*/
	public static final PropertyInteger PLANT = PropertyInteger.create("plant", 0, 9);
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	//0: Empty, 1: water, 2: lava, 3: coal, 4: glowstone, 5: reserved, 6: salt block, 7: poison potato
	public static final PropertyInteger BRAZIER_CONTENTS = PropertyInteger.create("br_cont", 0, 7);
	public static final PropertyBool CR_VERSION = PropertyBool.create("cr_version");
	public static final PropertyBool REDSTONE_BOOL = PropertyBool.create("redstone_bool");
	public static final PropertyBool HEAD = PropertyBool.create("head");
}
