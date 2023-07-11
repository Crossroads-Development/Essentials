package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.BrazierTileEntity;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

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
			e.register(ForgeRegistries.Keys.BLOCKS, ESBlocks::init);

			e.register(ForgeRegistries.Keys.ITEMS, ESItems::init);

			e.register(ForgeRegistries.Keys.ENTITY_TYPES, helper -> {
				helper.register("cannon_skull", WitherCannon.CannonSkull.ENT_TYPE);
			});

			e.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
				ESTileEntity.init();
				for(Map.Entry<String, BlockEntityType<?>> entry : ESTileEntity.toRegister.entrySet()){
					helper.register(entry.getKey(), entry.getValue());
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
				helper.register("core", ESItems.ESSENTIALS_TAB);
			});
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
			MenuType<T> contType = new MenuType<>(cons, FeatureFlags.VANILLA_SET);
			helper.register(id, contType);
			return contType;
		}
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void blockWitchSpawns(MobSpawnEvent.FinalizeSpawn e){
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
	public static void preventTeleport(EntityTeleportEvent e){
		if(e.getEntity() instanceof EnderMan enderman && enderman.level() instanceof ServerLevel world){
			int RANGE = ESConfig.brazierRange.get();
			int RANGE_SQUARED = (int) Math.pow(RANGE, 2);
			String dimKey = world.dimension().location().toString();
			HashSet<BlockPos> brazierPositions = BrazierTileEntity.BRAZIER_POSITIONS.get(dimKey);
			if(brazierPositions != null){
				for(BlockPos otherPos : brazierPositions){
					if(otherPos.distToCenterSqr(e.getPrevX(), e.getPrevY(), e.getPrevZ()) <= RANGE_SQUARED){
						BlockState state = world.getBlockState(otherPos);
						if(state.getBlock() == ESBlocks.brazier && state.getValue(ESProperties.BRAZIER_CONTENTS) == 6){
							e.setResult(Event.Result.DENY);
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
