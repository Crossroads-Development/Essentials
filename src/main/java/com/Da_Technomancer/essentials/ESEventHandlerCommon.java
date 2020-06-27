package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ESEventHandlerCommon{

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void blockWitchSpawns(LivingSpawnEvent e){
		//Prevents witch spawning if a nearby brazier has soulsand
		if(e.getEntity() instanceof WitchEntity){
			int RANGE = ESConfig.brazierRange.get();
			for(TileEntity te : e.getWorld().getWorld().tickableTileEntities){
				World w;
				//Mapping note: method with 0.5 offset, followed by method for comparing distances
				if(te instanceof BrazierTileEntity && Vector3d.func_237489_a_(te.getPos()).func_237488_a_(e.getEntity().getPositionVec(), RANGE) && (w = te.getWorld()) != null){
					BlockState state = w.getBlockState(te.getPos());
					if(state.getBlock() == ESBlocks.brazier && state.get(ESProperties.BRAZIER_CONTENTS) == 6){
						e.setResult(Event.Result.DENY);
						return;
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void preventTeleport(EnderTeleportEvent e){
		if(e.getEntity() instanceof EndermanEntity){
			int RANGE = ESConfig.brazierRange.get();
			for(TileEntity te : e.getEntity().getEntityWorld().tickableTileEntities){
				//Mapping note: method with 0.5 offset, followed by method for comparing distances
				if(te instanceof BrazierTileEntity && Vector3d.func_237489_a_(te.getPos()).func_237488_a_(e.getEntity().getPositionVec(), RANGE) && te.getWorld() != null){
					BlockState state = te.getBlockState();
					if(state.getBlock() == ESBlocks.brazier && state.get(ESProperties.BRAZIER_CONTENTS) == 6){
						e.setCanceled(true);
						return;
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void feedAnimal(PlayerInteractEvent.EntityInteract e){
		if(e.getTarget() instanceof AnimalEntity && e.getItemStack().getItem() == ESItems.animalFeed && (!(e.getTarget() instanceof TameableEntity) || ((TameableEntity) e.getTarget()).isTamed())){
			e.setResult(Event.Result.DENY);
			e.setCanceled(true);
			AnimalEntity an = (AnimalEntity) e.getTarget();
			if(!e.getWorld().isRemote && an.getGrowingAge() == 0 && an.canBreed()){
				an.setInLove(e.getPlayer());
				if(!e.getPlayer().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
		}
	}
}
