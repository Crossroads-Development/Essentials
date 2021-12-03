package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

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
	public static void sendClientPacketAround(Level world, BlockPos pos, Packet packet){
		EssentialsPackets.channel.send(PacketDistributor.NEAR.with(PacketDistributor.TargetPoint.p(pos.getX(), pos.getY(), pos.getZ(), 512, world.dimension())), packet);
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
		return ItemStack.isSame(a, b) && ItemStack.tagMatches(a, b);
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

	/**
	 * Gets a collection of loaded BlockEntities in the world which are nearby a specified position.
	 * The returned map will contain all loaded BlockEntities within range, and possibly several out of range
	 * The passed range should be used to ensure the returned collection is sufficient, not to enforce logic
	 * @param world The world
	 * @param centerPos The position to measure from
	 * @param range The range, in blocks, where all loaded block entities within range will definitely be returned
	 * @return A collection containing all relevant block entities, and possibly non-relevant ones as well
	 */
	public static Collection<BlockEntity> getAllLoadedBlockEntitiesRange(Level world, BlockPos centerPos, int range){
		ArrayList<BlockEntity> blockEntities = new ArrayList<>();
		//Each chunk has a list of loaded entities
		//We need to query that list from each chunk in range and combine them
		//We do not filter based on exact distance within that chunk, to speed up the method call
		int minChunkX = SectionPos.blockToSectionCoord(centerPos.getX() - range);
		int minChunkZ = SectionPos.blockToSectionCoord(centerPos.getZ() - range);
		int maxChunkX = SectionPos.blockToSectionCoord(centerPos.getX() + range);
		int maxChunkZ = SectionPos.blockToSectionCoord(centerPos.getZ() + range);
		for(int i = minChunkX; i <= maxChunkX; i++){
			for(int j = minChunkZ; j <= maxChunkZ; j++){
				BlockPos internalPos = new BlockPos(SectionPos.sectionToBlockCoord(i), centerPos.getY(), SectionPos.sectionToBlockCoord(j));
				try{
					if(world.isLoaded(internalPos)){
						//Loaded chunks only
						LevelChunk chunk = world.getChunk(i, j);
						blockEntities.addAll(chunk.getBlockEntities().values());
					}
				}catch(IllegalStateException e){
					Essentials.logger.catching(e);
				}
			}
		}
		return blockEntities;
	}
}
