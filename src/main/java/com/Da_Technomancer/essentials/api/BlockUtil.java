package com.Da_Technomancer.essentials.api;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;

public class BlockUtil{

	/**
	 * Sends a packet from the server to the client, to all players 'near' a position
	 * Only valid for packets on the Essentials channel
	 * @param world The world to target the packet in
	 * @param pos The target position the packet reception area is centered around
	 * @param packet The server->client packet to send
	 */
	public static void sendClientPacketAround(Level world, BlockPos pos, CustomPacketPayload packet){
		PacketDistributor.sendToPlayersNear((ServerLevel) world, null, pos.getX(), pos.getY(), pos.getZ(), 512, packet);
	}

	public static CompoundTag stackToNBT(ItemStack stack, HolderLookup.Provider registries){
		return (CompoundTag) stack.saveOptional(registries);
	}

	public static CompoundTag stackToNBT(FluidStack stack, HolderLookup.Provider registries){
		return (CompoundTag) stack.saveOptional(registries);
	}

	public static ItemStack nbtToItemStack(CompoundTag nbt, HolderLookup.Provider registries){
		return ItemStack.parseOptional(registries, nbt);
	}

	public static FluidStack nbtToFluidStack(CompoundTag nbt, HolderLookup.Provider registries){
		return FluidStack.parseOptional(registries, nbt);
	}

	public static void stackToBuffer(ItemStack stack, FriendlyByteBuf buf, HolderLookup.Provider registries){
		buf.writeNbt(stackToNBT(stack, registries));
	}

	public static void stackToBuffer(FluidStack stack, FriendlyByteBuf buf, HolderLookup.Provider registries){
		buf.writeNbt(stackToNBT(stack, registries));
	}

	public static ItemStack bufferToItemStack(FriendlyByteBuf buf, HolderLookup.Provider registries){
		return nbtToItemStack(buf.readNbt(), registries);
	}

	public static FluidStack bufferToFluidStack(FriendlyByteBuf buf, HolderLookup.Provider registries){
		return nbtToFluidStack(buf.readNbt(), registries);
	}

	public static <T extends Comparable<T>> T evaluateProperty(BlockState state, Property<T> prop, T fallback){
		if(state.hasProperty(prop)){
			return state.getValue(prop);
		}
		return fallback;
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
		return ItemStack.isSameItemSameComponents(a, b);
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
		return a.getFluid() == b.getFluid() && FluidStack.isSameFluidSameComponents(a, b);
	}

	/**
	 * Gets a collection of loaded BlockEntities in the world which are nearby a specified position.
	 * The returned map will contain all loaded BlockEntities within range, and possibly several out of range
	 * The passed range should be used to ensure the returned collection is sufficient, not to enforce logic
	 * @param world The world
	 * @param centerPos The position to measure from
	 * @param range The range, in blocks, where all loaded block entities within range will definitely be returned
	 * @return A collection containing all relevant block entities, and possibly non-relevant ones as well
	 * @deprecated Very laggy in some cases; find alternative solutions
	 */
	@Deprecated
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

	public static Component blockPosToChatComponent(BlockPos pos){
		return Component.translatable("tt.essentials.block_pos", pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	 * @param player The interacting player
	 * @param te The TE to be interacted with
	 * @return Whether to allow the player to interact with the UI of a machine
	 */
	public static boolean playerInRangeOfUI(Player player, BlockEntity te){
		return Container.stillValidBlockEntity(te, player, 8);
	}

	/**
	 * Use the version that takes a TE as parameter if applicable
	 * @param player The interacting player
	 * @param containerPos Position of the tile entity being interacted with
	 * @return Whether to allow the player to interact with the UI of a machine
	 */
	public static boolean playerInRangeOfUI(Player player, BlockPos containerPos){
		return player.canInteractWithBlock(containerPos, 8);
	}
}
