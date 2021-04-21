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
	public void blockWitchSpawns(LivingSpawnEvent.CheckSpawn e){
		//Prevents witch spawning if a nearby brazier has soulsand
		if(e.getEntity() instanceof WitchEntity && e.getWorld() instanceof World){
			int RANGE_SQUARED = (int) Math.pow(ESConfig.brazierRange.get(), 2);
			for(TileEntity te : ((World) e.getWorld()).tickableBlockEntities){
				World w;
				if(te instanceof BrazierTileEntity && te.getBlockPos().distSqr(e.getX(), e.getY(), e.getZ(), true) <= RANGE_SQUARED && (w = te.getLevel()) != null){
					BlockState state = w.getBlockState(te.getBlockPos());
					if(state.getBlock() == ESBlocks.brazier && state.getValue(ESProperties.BRAZIER_CONTENTS) == 6){
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
			int RANGE_SQUARED = (int) Math.pow(ESConfig.brazierRange.get(), 2);
			for(TileEntity te : e.getEntity().getCommandSenderWorld().tickableBlockEntities){
				Vector3d entPos = e.getEntity().position();
				if(te instanceof BrazierTileEntity && te.getBlockPos().distSqr(entPos, true) <= RANGE_SQUARED && te.getLevel() != null){
					BlockState state = te.getBlockState();
					if(state.getBlock() == ESBlocks.brazier && state.getValue(ESProperties.BRAZIER_CONTENTS) == 6){
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
		if(e.getTarget() instanceof AnimalEntity && e.getItemStack().getItem() == ESItems.animalFeed && (!(e.getTarget() instanceof TameableEntity) || ((TameableEntity) e.getTarget()).isTame())){
			e.setResult(Event.Result.DENY);
			e.setCanceled(true);
			AnimalEntity an = (AnimalEntity) e.getTarget();
			if(!e.getWorld().isClientSide && an.getAge() == 0){
				an.setInLove(e.getPlayer());
				if(!e.getPlayer().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
		}
	}
}
