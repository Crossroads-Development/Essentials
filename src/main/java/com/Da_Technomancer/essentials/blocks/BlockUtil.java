package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.Packet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

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

	/**
	 * Sends a packet from the server to the client, to all players 'near' a position
	 * Only valid for packets on the Essentials channel
	 * @param world The world to target the packet in
	 * @param pos The target position the packet reception area is centered around
	 * @param packet The server->client packet to be send. Essentials channel packets only.
	 */
	public static void sendClientPacketAround(World world, BlockPos pos, Packet packet){
		EssentialsPackets.channel.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), 512, world.getDimensionKey())), packet);
	}

	/**
	 * @param a The first itemstack to compare
	 * @param b The second itemstack to compare
	 * @return If the two itemstacks should be considered to have the same item and/or stack
	 */
	public static boolean sameItem(ItemStack a, ItemStack b){
		if(a == null || b == null){
			return false;
		}
		return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
	}

	/**
	 * @param a The first fluidstack to compare
	 * @param b The second fluidstack to compare
	 * @return If the two fluidstacks should be considered to have the same fluid and/or stack
	 */
	public static boolean sameFluid(FluidStack a, FluidStack b){
		if(a == null || b == null){
			return false;
		}
		return a.getFluid() == b.getFluid() && FluidStack.areFluidStackTagsEqual(a, b);
	}
}
