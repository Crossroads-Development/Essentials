package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EssentialsEventHandlerCommon{

	@SubscribeEvent
	public void onEntitySpawn(LivingSpawnEvent e){
		if(e.getEntity() instanceof EntityWitch){
			int RANGE_SQUARED = (int) Math.pow(EssentialsConfig.brazierRange.get(), 2);

			for(TileEntity te : e.getWorld().getWorld().tickableTileEntities){
				if(te instanceof BrazierTileEntity && te.getDistanceSq(e.getX(), e.getY(), e.getZ()) <= RANGE_SQUARED){
					IBlockState state = te.getWorld().getBlockState(te.getPos());
					if(state.getBlock() == EssentialsBlocks.brazier && state.get(EssentialsProperties.BRAZIER_CONTENTS) == 6){
						e.setResult(Event.Result.DENY);
						return;
					}
				}
			}
		}
	}
}
