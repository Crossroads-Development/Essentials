package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ESEventHandlerCommon{

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void blockWitchSpawns(LivingSpawnEvent.CheckSpawn e){
		//Prevents witch spawning if a nearby brazier has soulsand
		if(e.getEntity() instanceof Witch && e.getWorld() instanceof Level){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange((Level) e.getWorld(), e.getEntity().blockPosition(), RANGE)){
				Level w;
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
	public void preventTeleport(EntityTeleportEvent e){
		if(e.getEntity() instanceof EnderMan){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange(e.getEntity().getCommandSenderWorld(), e.getEntity().blockPosition(), RANGE)){
				Vec3 entPos = e.getEntity().position();
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
		if(e.getTarget() instanceof Animal && e.getItemStack().getItem() == ESItems.animalFeed && (!(e.getTarget() instanceof TamableAnimal) || ((TamableAnimal) e.getTarget()).isTame())){
			e.setResult(Event.Result.DENY);
			e.setCanceled(true);
			Animal an = (Animal) e.getTarget();
			if(!e.getWorld().isClientSide && an.getAge() == 0){
				an.setInLove(e.getPlayer());
				if(!e.getPlayer().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
		}
	}
}
