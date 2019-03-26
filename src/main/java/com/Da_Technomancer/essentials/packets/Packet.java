package com.Da_Technomancer.essentials.packets;

import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public abstract class Packet{

	//Required empty constructor for packets
	public Packet(){

	}

	/**
	 * Returns an array of all fields to be synced with the packet.
	 * The order of the array must not vary between calls or on different sides
	 * @return An array of all fields to sync
	 */
	@Nonnull
	protected abstract Field[] getFields();

	/**
	 * Performs the action of the packet on the receiving end
	 * @param context The packet context
	 */
	protected abstract void consume(NetworkEvent.Context context);

}
