package com.Da_Technomancer.essentials.api;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * For placement on tile entities which act as item containers
 */
public interface IItemContainer extends WorldlyContainer, IItemStorage{

	default int getMaxStackSize(int slot){
		return getMaxStackSize();
	}

	@Override
	default boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction){
		return canPlaceItem(index, stack);
	}

	/**
	 * Purely a convenience method
	 * @return A new PacketBuffer pre-formatted with standard InventoryContainer info
	 */
	default FriendlyByteBuf createContainerBuf(){
		return new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(((BlockEntity) this).getBlockPos());
	}
}
