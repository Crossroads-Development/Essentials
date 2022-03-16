package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.tileentities.*;
import com.Da_Technomancer.essentials.tileentities.redstone.*;
import com.mojang.datafixers.DSL;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.IForgeRegistry;

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
		public static void registerBlocks(RegistryEvent.Register<Block> e){
			IForgeRegistry<Block> registry = e.getRegistry();
			ESBlocks.init();
			for(Block block : toRegister){
				registry.register(block);
			}
			toRegister.clear();
		}

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void registerItems(RegistryEvent.Register<Item> e){
			IForgeRegistry<Item> registry = e.getRegistry();
			ESItems.init();
			for(Item item : ESItems.toRegister){
				registry.register(item);
			}
			ESItems.toRegister.clear();
		}

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void registerEnts(RegistryEvent.Register<EntityType<?>> e){
			IForgeRegistry<EntityType<?>> registry = e.getRegistry();
			registry.register(EntityType.Builder.of(WitherCannon.CannonSkull::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).sized(0.3125F, 0.3125F).fireImmune().setUpdateInterval(4).setTrackingRange(4).setCustomClientFactory((PlayMessages.SpawnEntity s, Level w) -> new WitherCannon.CannonSkull(WitherCannon.ENT_TYPE, w)).build("cannon_skull").setRegistryName(Essentials.MODID, "cannon_skull"));
		}

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> e){
			IForgeRegistry<BlockEntityType<?>> reg = e.getRegistry();
			registerTE(BrazierTileEntity::new, "brazier", reg, brazier);
			registerTE(SlottedChestTileEntity::new, "slotted_chest", reg, slottedChest);
			registerTE(SortingHopperTileEntity::new, "sorting_hopper", reg, sortingHopper);
			registerTE(SpeedHopperTileEntity::new, "speed_hopper", reg, speedHopper);
			registerTE(ItemShifterTileEntity::new, "item_shifter", reg, itemShifter);
			registerTE(FluidShifterTileEntity::new, "fluid_shifter", reg, fluidShifter);
			registerTE(HopperFilterTileEntity::new, "hopper_filter", reg, hopperFilter);
			registerTE(BasicItemSplitterTileEntity::new, "basic_item_splitter", reg, basicItemSplitter);
			registerTE(ItemSplitterTileEntity::new, "item_splitter", reg, itemSplitter);
			registerTE(BasicFluidSplitterTileEntity::new, "basic_fluid_splitter", reg, basicFluidSplitter);
			registerTE(FluidSplitterTileEntity::new, "fluid_splitter", reg, fluidSplitter);
			registerTE(CircuitTileEntity::new, "circuit", reg, andCircuit, orCircuit, interfaceCircuit, notCircuit, xorCircuit, maxCircuit, minCircuit, sumCircuit, difCircuit, prodCircuit, quotCircuit, powCircuit, invCircuit, cosCircuit, sinCircuit, tanCircuit, asinCircuit, acosCircuit, atanCircuit, readerCircuit, moduloCircuit, moreCircuit, lessCircuit, equalsCircuit, absCircuit, signCircuit);
			registerTE(ConstantCircuitTileEntity::new, "cons_circuit", reg, consCircuit);
			registerTE(TimerCircuitTileEntity::new, "timer_circuit", reg, timerCircuit);
			registerTE(DelayCircuitTileEntity::new, "delay_circuit", reg, delayCircuit);
			registerTE(WireTileEntity::new, "wire", reg, wireCircuit);
			registerTE(WireJunctionTileEntity::new, "wire_junction", reg, wireJunctionCircuit);
			registerTE(AutoCrafterTileEntity::new, "auto_crafter", reg, autoCrafter);
			registerTE(RedstoneTransmitterTileEntity::new, "redstone_transmitter", reg, redstoneTransmitter);
			registerTE(RedstoneReceiverTileEntity::new, "redstone_receiver", reg, redstoneReceiver);
			registerTE(PulseCircuitTileEntity::new, "pulse_circuit", reg, pulseCircuitRising, pulseCircuitFalling, pulseCircuitDual);
			registerTE(DCounterCircuitTileEntity::new, "d_counter_circuit", reg, dCounterCircuit);
		}

		private static void registerTE(BlockEntityType.BlockEntitySupplier<? extends BlockEntity> cons, String id, IForgeRegistry<BlockEntityType<?>> reg, Block... blocks){
			BlockEntityType<?> teType = BlockEntityType.Builder.of(cons, blocks).build(DSL.emptyPartType());
			teType.setRegistryName(new ResourceLocation(Essentials.MODID, id));
			reg.register(teType);
		}

		/**
		 * Creates and registers a container type
		 * @param cons Container factory
		 * @param id The ID to use
		 * @param reg Registery event
		 * @param <T> Container subclass
		 * @return The newly created type
		 */
		protected static <T extends AbstractContainerMenu> MenuType<T> registerConType(IContainerFactory<T> cons, String id, RegistryEvent.Register<MenuType<?>> reg){
			MenuType<T> contType = new MenuType<>(cons);
			contType.setRegistryName(new ResourceLocation(Essentials.MODID, id));
			reg.getRegistry().register(contType);
			return contType;
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void blockWitchSpawns(LivingSpawnEvent.CheckSpawn e){
		//Prevents witch spawning if a nearby brazier has soulsand
		if(e.getEntity() instanceof Witch && e.getWorld() instanceof Level){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange((Level) e.getWorld(), e.getEntity().blockPosition(), RANGE)){
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
			if(!e.getWorld().isClientSide && an.getAge() == 0){
				an.setInLove(e.getPlayer());
				if(!e.getPlayer().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
		}
	}
}
