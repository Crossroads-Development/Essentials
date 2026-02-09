package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.IFluidCapable;
import com.Da_Technomancer.essentials.api.IItemCapable;
import com.Da_Technomancer.essentials.api.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.api.redstone.IRedstoneCapable;
import com.Da_Technomancer.essentials.api.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.BrazierTileEntity;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.integration.ESIntegration;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

public class ESEventHandlerCommon{

	@EventBusSubscriber(modid = Essentials.MODID)
	public static class ESModEventsCommon{

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent e){
			e.registerBlock(Capabilities.ItemHandler.BLOCK, IItemCapable.CAPABLE_PROVIDER, ESBlocks.sortingHopper, ESBlocks.speedHopper, ESBlocks.hopperFilter, ESBlocks.itemShifter, ESBlocks.basicItemSplitter, ESBlocks.itemSplitter, ESBlocks.slottedChest, ESBlocks.hopperFilter, ESBlocks.brazier);
			e.registerBlock(Capabilities.FluidHandler.BLOCK, IFluidCapable.CAPABLE_PROVIDER, ESBlocks.fluidShifter, ESBlocks.basicFluidSplitter, ESBlocks.fluidSplitter);
			e.registerBlock(IRedstoneHandler.REDS_HANDLER_BLOCK, IRedstoneCapable.CAPABLE_PROVIDER, ESBlocks.wireCircuit, ESBlocks.wireJunctionCircuit, ESBlocks.consCircuit, ESBlocks.interfaceCircuit, ESBlocks.andCircuit, ESBlocks.notCircuit, ESBlocks.orCircuit, ESBlocks.xorCircuit, ESBlocks.maxCircuit, ESBlocks.minCircuit, ESBlocks.sumCircuit, ESBlocks.difCircuit, ESBlocks.prodCircuit, ESBlocks.quotCircuit, ESBlocks.powCircuit, ESBlocks.invCircuit, ESBlocks.sinCircuit, ESBlocks.cosCircuit, ESBlocks.tanCircuit, ESBlocks.asinCircuit, ESBlocks.acosCircuit, ESBlocks.atanCircuit, ESBlocks.equalsCircuit, ESBlocks.lessCircuit, ESBlocks.moreCircuit, ESBlocks.roundCircuit, ESBlocks.floorCircuit, ESBlocks.ceilCircuit, ESBlocks.logCircuit, ESBlocks.moduloCircuit, ESBlocks.absCircuit, ESBlocks.signCircuit, ESBlocks.readerCircuit, ESBlocks.timerCircuit, ESBlocks.timerCircuit, ESBlocks.delayCircuit, ESBlocks.pulseCircuitRising, ESBlocks.pulseCircuitFalling, ESBlocks.pulseCircuitDual, ESBlocks.dCounterCircuit, ESBlocks.redstoneTransmitter, ESBlocks.redstoneReceiver);
			ESIntegration.initCapabilities(e);
		}

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void register(RegisterEvent e){
			e.register(Registries.BLOCK, ESBlocks::registerBlocks);

			e.register(Registries.ITEM, ESItems::registerItems);

			e.register(Registries.ENTITY_TYPE, helper -> {
				helper.register(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, "cannon_skull"), WitherCannon.CannonSkull.ENT_TYPE);
			});

			e.register(Registries.BLOCK_ENTITY_TYPE, helper -> {
				ESTileEntity.init();
				for(Map.Entry<String, BlockEntityType<?>> entry : ESTileEntity.toRegister.entrySet()){
					helper.register(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, entry.getKey()), entry.getValue());
				}
				ESTileEntity.toRegister.clear();
			});

			e.register(Registries.CREATIVE_MODE_TAB, helper -> {
				ESItems.ESSENTIALS_TAB = CreativeModeTab.builder()
						.title(Component.translatable("item_group." + Essentials.MODID))
						.icon(() -> new ItemStack(ESItems.itemCandleLilypad))
						.displayItems((params, output) -> {
									for(Supplier<ItemStack[]> itemsToAdd : ESItems.creativeTabItems){
										for(ItemStack itemToAdd : itemsToAdd.get()){
											output.accept(itemToAdd);
										}
									}
								}
						).build();
				helper.register(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, "core"), ESItems.ESSENTIALS_TAB);
			});
		}

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void register(RegisterPayloadHandlersEvent e){
			EssentialsPackets.init(e);
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void blockWitchSpawns(FinalizeSpawnEvent e){
		//Prevents witch spawning if a nearby brazier has soulsand
		if(e.getEntity() instanceof Witch && e.getLevel() instanceof ServerLevel world){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			String dimKey = world.dimension().location().toString();
			HashSet<BlockPos> brazierPositions = BrazierTileEntity.BRAZIER_POSITIONS.get(dimKey);
			if(brazierPositions != null){
				for(BlockPos otherPos : brazierPositions){
					if(otherPos.distToCenterSqr(e.getX(), e.getY(), e.getZ()) <= RANGE_SQUARED){
						BlockState state = world.getBlockState(otherPos);
						if(state.getBlock() == ESBlocks.brazier && state.getValue(ESProperties.BRAZIER_CONTENTS) == 6){
							e.setSpawnCancelled(true);
							return;
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void preventTeleport(EntityTeleportEvent.EnderEntity e){
		if(e.getEntityLiving().level() instanceof ServerLevel world){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			String dimKey = world.dimension().location().toString();
			HashSet<BlockPos> brazierPositions = BrazierTileEntity.BRAZIER_POSITIONS.get(dimKey);
			if(brazierPositions != null){
				for(BlockPos otherPos : brazierPositions){
					if(otherPos.distToCenterSqr(e.getPrevX(), e.getPrevY(), e.getPrevZ()) <= RANGE_SQUARED){
						BlockState state = world.getBlockState(otherPos);
						if(state.getBlock() == ESBlocks.brazier && state.getValue(ESProperties.BRAZIER_CONTENTS) == 6){
							e.setCanceled(true);
							return;
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void feedAnimal(PlayerInteractEvent.EntityInteract e){
		if(e.getTarget() instanceof Animal an && e.getItemStack().getItem() == ESItems.animalFeed && (!(e.getTarget() instanceof TamableAnimal) || ((TamableAnimal) e.getTarget()).isTame())){
			e.setCanceled(true);
			if(!e.getLevel().isClientSide && an.getAge() == 0){
				an.setInLove(e.getEntity());
				if(!e.getEntity().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
		}
	}

	private static final TagKey<Item> LILYPAD_LIGHT = ItemTags.create(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, "light_on_lilypad"));

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void placeCandleOnLilypad(UseItemOnBlockEvent e){
		if(e.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_BEFORE_BLOCK && e.getItemStack().is(LILYPAD_LIGHT) && e.getLevel().getBlockState(e.getPos()).getBlock() == Blocks.LILY_PAD){
			if(!e.getLevel().isClientSide){
				e.getLevel().setBlockAndUpdate(e.getPos(), ESBlocks.candleLilyPad.defaultBlockState());
				if(e.getPlayer() == null || !e.getPlayer().isCreative()){
					e.getItemStack().shrink(1);
				}
			}
			e.cancelWithResult(ItemInteractionResult.sidedSuccess(e.getLevel().isClientSide));
		}
	}
}
