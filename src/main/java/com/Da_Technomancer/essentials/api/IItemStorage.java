package com.Da_Technomancer.essentials.api;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;

/**
 * For placement on tile entities which store items that need to be dropped when the block is broken, where the block is a TEBlock.
 * Unneeded if the TE already implements Container
 */
public interface IItemStorage{

	/**
	 * Drop the items stored in this TE into the world; not necessary to clear the TE contents.
	 * @param world Current world. Should match TE world.
	 * @param pos Current position. Should match TE pos.
	 */
	default void dropItems(Level world, BlockPos pos){
		if(this instanceof Container cont){
			Containers.dropContents(world, pos, cont);
		}else{
			Essentials.logger.warn("Unable to drop items for " + this.toString() + "; report to mod author");
		}
	}
}
