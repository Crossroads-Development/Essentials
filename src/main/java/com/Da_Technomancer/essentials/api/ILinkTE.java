package com.Da_Technomancer.essentials.api;

import com.Da_Technomancer.essentials.api.packets.ILongReceiver;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Set;

/**
 * A helper class to be placed on TileEntities to enable basic linking behaviour
 */
public interface ILinkTE extends ILongReceiver{

	/**
	 * @return Whether this device can ever be the start of a link
	 */
	boolean canBeginLinking();

	/**
	 *
	 * @param otherTE The ILinkTE to attempt linking to
	 * @return Whether this TE is allowed to link to the otherTE. The source TE controls whether the link is allowed, the target is not checked. Do not check range
	 */
	boolean canLink(ILinkTE otherTE);

	/**
	 * A mutable list of linked relative block positions.
	 * @return The list of links
	 */
	Set<BlockPos> getLinks();

	/**
	 * Creates a link. Called on the source, before createLinkEnd
	 * @param endpoint The endpoint that this is being linked to
	 * @param player The calling player, for sending chat messages
	 * @return Whether the operation succeeded; returning false will cancel the link
	 */
	boolean createLinkSource(ILinkTE endpoint, @Nullable Player player);

	/**
	 * Called on the endpoint when a link is created
	 * @param src The source this is being linked to
	 */
	default void createLinkEnd(ILinkTE src){

	}

	/**
	 * Called on the origin of a link when the link is removed. Should remove the link.
	 * Called before removeLinkEnd, if applicable. The other method will still be called even if this ILinkTE no longer exists.
	 * @param end The relative position that this was linked to
	 */
	void removeLinkSource(BlockPos end);

	/**
	 * Called on the endpoint of a link when the link is removed.
	 * Called after removeLinkSource, if applicable. The other method will still be called even if this ILinkTE no longer exists.
	 * @param src The absolute position that this was linked to
	 */
	default void removeLinkEnd(BlockPos src){

	}

	/**
	 * @return This TE
	 */
	default BlockEntity getTE(){
		return (BlockEntity) this;
	}

	default int getRange(){
		return 16;
	}

	/**
	 * @return The maximum number of linked devices. The source controls this
	 */
	default int getMaxLinks(){
		return 3;
	}

	/**
	 * Gets the color to render link lines from this block
	 * Alpha is ignored
	 * @return Color of link lines from this block
	 */
	default Color getColor(){
		return Color.RED;
	}
}
