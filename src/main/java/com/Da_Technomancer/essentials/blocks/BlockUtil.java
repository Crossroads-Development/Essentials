package com.Da_Technomancer.essentials.blocks;

import jdk.internal.jline.internal.Nullable;
import net.minecraftforge.common.util.LazyOptional;

public class BlockUtil{

	/**
	 * Gets the value contained in a LazyOptional
	 * @param optional The LazyOptional
	 * @param <T> The type parameter of the LazyOptional
	 * @return The value contained in the LazyOptional, or null if the LazyOptional is empty
	 */
	public static <T> T get(@Nullable LazyOptional<T> optional){
		return optional != null && optional.isPresent() ? optional.orElseThrow(NullPointerException::new) : null;
	}
}
