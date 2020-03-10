package com.Da_Technomancer.essentials.gui.container;

import net.minecraft.util.IntReferenceHolder;

import java.util.function.Supplier;

/**
 * An int reference holder that uses a supplier to define the "true" value on the virtual server side
 * Typically used to reference a field
 */
public class IntDeferredRef extends IntReferenceHolder{

	private int setVal;
	private final Supplier<Integer> src;

	public IntDeferredRef(Supplier<Integer> source){
//		setVal = source.get();
		src = source;
	}

	@Override
	public int get(){
		return setVal;
	}

	@Override
	public void set(int i){
		setVal = i;
	}

	@Override
	public boolean isDirty(){
		int prev = setVal;
		int curr = src.get();
		boolean dirty = prev != curr;
		setVal = curr;
		return dirty;
	}
}
