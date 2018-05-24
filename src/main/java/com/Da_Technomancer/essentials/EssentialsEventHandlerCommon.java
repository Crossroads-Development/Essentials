package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EssentialsEventHandlerCommon{

	@SubscribeEvent
	public void onEntitySpawn(LivingSpawnEvent e){
		if(e.getEntity() instanceof EntityWitch){
			int RANGE_SQUARED = (int) Math.pow(EssentialsConfig.getConfigInt(EssentialsConfig.brazierRange, e.getWorld().isRemote), 2);

			for(TileEntity te : e.getWorld().tickableTileEntities){
				if(te instanceof BrazierTileEntity && te.getDistanceSq(e.getX(), e.getY(), e.getZ()) <= RANGE_SQUARED){
					IBlockState state = te.getWorld().getBlockState(te.getPos());
					if(state.getBlock() == EssentialsBlocks.brazier && state.getValue(EssentialsProperties.BRAZIER_CONTENTS) == 6){
						e.setResult(Event.Result.DENY);
						return;
					}
				}
			}
		}
	}
}
