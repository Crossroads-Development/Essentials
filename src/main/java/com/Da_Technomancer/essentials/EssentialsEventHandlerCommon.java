package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EssentialsEventHandlerCommon{

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void onEntitySpawn(LivingSpawnEvent e){
		if(e.getEntity() instanceof WitchEntity){
			int RANGE_SQUARED = (int) Math.pow(EssentialsConfig.brazierRange.get(), 2);

			for(TileEntity te : e.getWorld().getWorld().tickableTileEntities){
				if(te instanceof BrazierTileEntity && te.getDistanceSq(e.getX(), e.getY(), e.getZ()) <= RANGE_SQUARED){
					BlockState state = te.getWorld().getBlockState(te.getPos());
					if(state.getBlock() == EssentialsBlocks.brazier && state.get(EssentialsProperties.BRAZIER_CONTENTS) == 6){
						e.setResult(Event.Result.DENY);
						return;
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void feedAnimal(PlayerInteractEvent.EntityInteract e){
		if(e.getTarget() instanceof AnimalEntity && e.getItemStack().getItem() == EssentialsItems.animalFeed && (!(e.getTarget() instanceof TameableEntity) || ((TameableEntity) e.getTarget()).isTamed())){
			e.setResult(Event.Result.DENY);
			e.setCanceled(true);
			AnimalEntity an = (AnimalEntity) e.getTarget();
			if(!e.getWorld().isRemote && an.getGrowingAge() == 0 && an.canBreed()){
				an.setInLove(e.getEntityPlayer());
				if(!e.getEntityPlayer().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
		}
	}
}
