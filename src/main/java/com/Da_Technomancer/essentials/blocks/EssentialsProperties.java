package com.Da_Technomancer.essentials.blocks;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;

public class EssentialsProperties{

	/**0 = wheat, 1 = potato, 2 = carrots, 3 = beetroot, 4 = oak, 5 = birch, 6 = spruce, 7 = jungle, 8 = acacia, 9 = dark oak*/
	public static final PropertyInteger PLANT = PropertyInteger.create("plant", 0, 9);
	public static final PropertyDirection FACING = PropertyDirection.create("facing");

}
