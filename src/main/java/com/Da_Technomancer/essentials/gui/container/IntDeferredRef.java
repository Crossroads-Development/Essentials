package com.Da_Technomancer.essentials.gui.container;

import net.minecraft.world.inventory.DataSlot;

import java.util.function.Supplier;

/**
 * An int reference holder that uses a supplier to define the "true" value on the virtual server side
 * Typically used to reference a field
 */
public class IntDeferredRef extends DataSlot{

	private Integer setVal = null;//We use an Integer to allow a null value, which indicates that no value has been initialized or set yet
	private final Supplier<Integer> src;
	private final boolean isRemote;

	public IntDeferredRef(Supplier<Integer> source, boolean isRemote){
//		setVal = source.get();
		src = source;
		this.isRemote = isRemote;
	}

	@Override
	public int get(){
		return setVal == null ? 0 : setVal;
	}

	@Override
	public void set(int i){
		setVal = i;
	}

	@Override
	public boolean checkAndClearUpdateFlag(){
		if(isRemote){
			return false;//This shouldn't ever be called on the client side, but if it were, this isn't the side that should originate updates
		}

		boolean dirty;
		if(setVal == null){
			dirty = true;//No value has been initialized on either side, we must be in need of a resync
		}else{
			//On the server side, take the supplier as the 'true' value
			dirty = (int) setVal != src.get();
		}
		setVal = src.get();//Update the last value synced to clients

		return dirty;
	}
}
