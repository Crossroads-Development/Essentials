package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.*;
import com.Da_Technomancer.essentials.blocks.redstone.*;
import com.Da_Technomancer.essentials.items.ESItems;
import com.mojang.datafixers.DSL;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Map;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.*;

public class ESEventHandlerCommon{

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Essentials.MODID)
	public static class ESModEventsCommon{

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent e){
			e.register(IRedstoneHandler.class);
		}

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void register(RegisterEvent e){
			e.register(ForgeRegistries.Keys.BLOCKS, helper -> {
				ESBlocks.init();
				for(Map.Entry<String, Block> block : toRegister.entrySet()){
					helper.register(block.getKey(), block.getValue());
				}
				toRegister.clear();
			});
		
			e.register(ForgeRegistries.Keys.ITEMS, helper -> {
				ESItems.init();
				for(Map.Entry<String, Item> item : ESItems.toRegister.entrySet()){
					helper.register(item.getKey(), item.getValue());
				}
				ESItems.toRegister.clear();
			});
		
			e.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
				helper.register("cannon_skull", EntityType.Builder.of(WitherCannon.CannonSkull::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).sized(0.3125F, 0.3125F).fireImmune().setUpdateInterval(4).setTrackingRange(4).setCustomClientFactory((PlayMessages.SpawnEntity s, Level w) -> new WitherCannon.CannonSkull(WitherCannon.ENT_TYPE, w)).build("cannon_skull"));
			});
			
			e.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
				registerTE(BrazierTileEntity::new, "brazier", helper, brazier);
				registerTE(SlottedChestTileEntity::new, "slotted_chest", helper, slottedChest);
				registerTE(SortingHopperTileEntity::new, "sorting_hopper", helper, sortingHopper);
				registerTE(SpeedHopperTileEntity::new, "speed_hopper", helper, speedHopper);
				registerTE(ItemShifterTileEntity::new, "item_shifter", helper, itemShifter);
				registerTE(FluidShifterTileEntity::new, "fluid_shifter", helper, fluidShifter);
				registerTE(HopperFilterTileEntity::new, "hopper_filter", helper, hopperFilter);
				registerTE(BasicItemSplitterTileEntity::new, "basic_item_splitter", helper, basicItemSplitter);
				registerTE(ItemSplitterTileEntity::new, "item_splitter", helper, itemSplitter);
				registerTE(BasicFluidSplitterTileEntity::new, "basic_fluid_splitter", helper, basicFluidSplitter);
				registerTE(FluidSplitterTileEntity::new, "fluid_splitter", helper, fluidSplitter);
				registerTE(CircuitTileEntity::new, "circuit", helper, andCircuit, orCircuit, notCircuit, xorCircuit, maxCircuit, minCircuit, sumCircuit, difCircuit, prodCircuit, quotCircuit, powCircuit, invCircuit, cosCircuit, sinCircuit, tanCircuit, asinCircuit, acosCircuit, atanCircuit, readerCircuit, moduloCircuit, moreCircuit, lessCircuit, equalsCircuit, absCircuit, signCircuit);
				registerTE(ConstantCircuitTileEntity::new, "cons_circuit", helper, consCircuit);
				registerTE(TimerCircuitTileEntity::new, "timer_circuit", helper, timerCircuit);
				registerTE(DelayCircuitTileEntity::new, "delay_circuit", helper, delayCircuit);
				registerTE(WireTileEntity::new, "wire", helper, wireCircuit);
				registerTE(WireJunctionTileEntity::new, "wire_junction", helper, wireJunctionCircuit);
				registerTE(AutoCrafterTileEntity::new, "auto_crafter", helper, autoCrafter);
				registerTE(RedstoneTransmitterTileEntity::new, "redstone_transmitter", helper, redstoneTransmitter);
				registerTE(RedstoneReceiverTileEntity::new, "redstone_receiver", helper, redstoneReceiver);
				registerTE(PulseCircuitTileEntity::new, "pulse_circuit", helper, pulseCircuitRising, pulseCircuitFalling, pulseCircuitDual);
				registerTE(DCounterCircuitTileEntity::new, "d_counter_circuit", helper, dCounterCircuit);
				registerTE(InterfaceCircuitTileEntity::new, "interface_circuit", helper, interfaceCircuit);
			});
		}

		private static void registerTE(BlockEntityType.BlockEntitySupplier<? extends BlockEntity> cons, String id, RegisterEvent.RegisterHelper<BlockEntityType<?>> helper, Block... blocks){
			BlockEntityType<?> teType = BlockEntityType.Builder.of(cons, blocks).build(DSL.emptyPartType());
			helper.register(id, teType);
		}

		/**
		 * Creates and registers a container type
		 * @param cons Container factory
		 * @param id The ID to use
		 * @param helper Registry helper
		 * @param <T> Container subclass
		 * @return The newly created type
		 */
		protected static <T extends AbstractContainerMenu> MenuType<T> registerConType(IContainerFactory<T> cons, String id, RegisterEvent.RegisterHelper<MenuType<?>> helper){
			MenuType<T> contType = new MenuType<>(cons);
			helper.register(id, contType);
			return contType;
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void blockWitchSpawns(LivingSpawnEvent.CheckSpawn e){
		//Prevents witch spawning if a nearby brazier has soulsand
		if(e.getEntity() instanceof Witch && e.getLevel() instanceof Level){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange((Level) e.getLevel(), e.getEntity().blockPosition(), RANGE)){
				Level w;
				if(te instanceof BrazierTileEntity && te.getBlockPos().distToCenterSqr(e.getX(), e.getY(), e.getZ()) <= RANGE_SQUARED && (w = te.getLevel()) != null){
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
	public static void preventTeleport(EntityTeleportEvent e){
		if(e.getEntity() instanceof EnderMan){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange(e.getEntity().getCommandSenderWorld(), e.getEntity().blockPosition(), RANGE)){
				Vec3 entPos = e.getEntity().position();
				if(te instanceof BrazierTileEntity && te.getBlockPos().distToCenterSqr(entPos) <= RANGE_SQUARED && te.getLevel() != null){
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
	public static void feedAnimal(PlayerInteractEvent.EntityInteract e){
		if(e.getTarget() instanceof Animal an && e.getItemStack().getItem() == ESItems.animalFeed && (!(e.getTarget() instanceof TamableAnimal) || ((TamableAnimal) e.getTarget()).isTame())){
			e.setResult(Event.Result.DENY);
			e.setCanceled(true);
			if(!e.getLevel().isClientSide && an.getAge() == 0){
				an.setInLove(e.getEntity());
				if(!e.getEntity().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
		}
	}
}
