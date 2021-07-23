package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.*;
import com.Da_Technomancer.essentials.gui.container.*;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import com.Da_Technomancer.essentials.tileentities.*;
import com.Da_Technomancer.essentials.tileentities.redstone.*;
import com.mojang.datafixers.DSL;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.tileentity.BlockEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static com.Da_Technomancer.essentials.Essentials.MODID;
import static com.Da_Technomancer.essentials.blocks.ESBlocks.*;

@Mod(MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Essentials{

	public static final String MODID = "essentials";
	public static final String MODNAME = "Essentials";
	public static final Logger logger = LogManager.getLogger(MODNAME);

	public Essentials(){
		final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonInit);
		bus.addListener(this::clientInit);

		ESConfig.init();

		MinecraftForge.EVENT_BUS.register(this);

		ESConfig.load();
	}

	private void commonInit(@SuppressWarnings("unused") FMLCommonSetupEvent e){
		//Pre
		EssentialsPackets.preInit();
		RedstoneUtil.registerCap();
		//Main
		MinecraftForge.EVENT_BUS.register(new ESEventHandlerCommon());
	}

	private void clientInit(@SuppressWarnings("unused") FMLClientSetupEvent e){
		TESRRegistry.init();
		MinecraftForge.EVENT_BUS.register(new ESEventHandlerClient());
		RenderTypeLookup.setRenderLayer(hopperFilter, RenderType.cutout());
		RenderTypeLookup.setRenderLayer(candleLilyPad, RenderType.cutout());
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent e){
		RenderingRegistry.registerEntityRenderingHandler(WitherCannon.ENT_TYPE, WitherSkullRenderer::new);
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
		registry.register(EntityType.Builder.of(WitherCannon.CannonSkull::new, EntityClassification.MISC).setShouldReceiveVelocityUpdates(true).sized(0.3125F, 0.3125F).fireImmune().setUpdateInterval(4).setTrackingRange(4).setCustomClientFactory((FMLPlayMessages.SpawnEntity s, Level w) -> new WitherCannon.CannonSkull(WitherCannon.ENT_TYPE, w)).build("cannon_skull").setRegistryName(Essentials.MODID, "cannon_skull"));
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<BlockEntityType<?>> e){
		IForgeRegistry<BlockEntityType<?>> reg = e.getRegistry();
		registerTE(BrazierBlockEntity::new, "brazier", reg, brazier);
		registerTE(SlottedChestBlockEntity::new, "slotted_chest", reg, slottedChest);
		registerTE(SortingHopperBlockEntity::new, "sorting_hopper", reg, sortingHopper);
		registerTE(SpeedHopperBlockEntity::new, "speed_hopper", reg, speedHopper);
		registerTE(ItemShifterBlockEntity::new, "item_shifter", reg, itemShifter);
		registerTE(FluidShifterBlockEntity::new, "fluid_shifter", reg, fluidShifter);
		registerTE(HopperFilterBlockEntity::new, "hopper_filter", reg, hopperFilter);
		registerTE(BasicItemSplitterBlockEntity::new, "basic_item_splitter", reg, basicItemSplitter);
		registerTE(ItemSplitterBlockEntity::new, "item_splitter", reg, itemSplitter);
		registerTE(BasicFluidSplitterBlockEntity::new, "basic_fluid_splitter", reg, basicFluidSplitter);
		registerTE(FluidSplitterBlockEntity::new, "fluid_splitter", reg, fluidSplitter);
		registerTE(CircuitBlockEntity::new, "circuit", reg, andCircuit, orCircuit, interfaceCircuit, notCircuit, xorCircuit, maxCircuit, minCircuit, sumCircuit, difCircuit, prodCircuit, quotCircuit, powCircuit, invCircuit, cosCircuit, sinCircuit, tanCircuit, asinCircuit, acosCircuit, atanCircuit, readerCircuit, moduloCircuit, moreCircuit, lessCircuit, equalsCircuit, absCircuit, signCircuit);
		registerTE(ConstantCircuitBlockEntity::new, "cons_circuit", reg, consCircuit);
		registerTE(TimerCircuitBlockEntity::new, "timer_circuit", reg, timerCircuit);
		registerTE(DelayCircuitBlockEntity::new, "delay_circuit", reg, delayCircuit);
		registerTE(WireBlockEntity::new, "wire", reg, wireCircuit);
		registerTE(WireJunctionBlockEntity::new, "wire_junction", reg, wireJunctionCircuit);
		registerTE(AutoCrafterBlockEntity::new, "auto_crafter", reg, autoCrafter);
		registerTE(RedstoneTransmitterBlockEntity::new, "redstone_transmitter", reg, redstoneTransmitter);
		registerTE(RedstoneReceiverBlockEntity::new, "redstone_receiver", reg, redstoneReceiver);
		registerTE(PulseCircuitBlockEntity::new, "pulse_circuit", reg, pulseCircuitRising, pulseCircuitFalling, pulseCircuitDual);
	}

	private static void registerTE(Supplier<? extends BlockEntity> cons, String id, IForgeRegistry<BlockEntityType<?>> reg, Block... blocks){
		BlockEntityType<?> teType = BlockEntityType.Builder.of(cons, blocks).build(DSL.emptyPartType());
		teType.setRegistryName(new ResourceLocation(MODID, id));
		reg.register(teType);
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	@OnlyIn(Dist.CLIENT)
	public static void registerContainers(RegistryEvent.Register<ContainerType<?>> e){
		registerCon(ItemShifterContainer::new, ItemShifterScreen::new, "item_shifter", e);
		registerCon(FluidShifterContainer::new, FluidShifterScreen::new, "fluid_shifter", e);
		registerCon(SlottedChestContainer::new, SlottedChestScreen::new, "slotted_chest", e);
		registerCon(CircuitWrenchContainer::new, CircuitWrenchScreen::new, "circuit_wrench", e);
		registerCon(ConstantCircuitContainer::new, ConstantCircuitScreen::new, "cons_circuit", e);
		registerCon(TimerCircuitContainer::new, TimerCircuitScreen::new, "timer_circuit", e);
		registerCon(AutoCrafterContainer::new, AutoCrafterScreen::new, "auto_crafter", e);
		registerCon(DelayCircuitContainer::new, DelayCircuitScreen::new, "delay_circuit", e);
		registerCon(PulseCircuitContainer::new, PulseCircuitScreen::new, "pulse_circuit", e);
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	@OnlyIn(Dist.DEDICATED_SERVER)
	public static void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> e){
		registerConType(ItemShifterContainer::new, "item_shifter", e);
		registerConType(FluidShifterContainer::new, "fluid_shifter", e);
		registerConType(SlottedChestContainer::new, "slotted_chest", e);
		registerConType(CircuitWrenchContainer::new, "circuit_wrench", e);
		registerConType(ConstantCircuitContainer::new, "cons_circuit", e);
		registerConType(TimerCircuitContainer::new, "timer_circuit", e);
		registerConType(AutoCrafterContainer::new, "auto_crafter", e);
		registerConType(DelayCircuitContainer::new, "delay_circuit", e);
		registerConType(PulseCircuitContainer::new, "pulse_circuit", e);
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	@OnlyIn(Dist.CLIENT)
	public static void onTextureStitch(TextureStitchEvent.Pre event){
		//Add textures used in TESRs
		//Currently none used
	}

	/**
	 * Creates and registers a container type
	 * @param cons Container factory
	 * @param id The ID to use
	 * @param reg Registery event
	 * @param <T> Container subclass
	 * @return The newly created type
	 */
	private static <T extends Container> ContainerType<T> registerConType(IContainerFactory<T> cons, String id, RegistryEvent.Register<ContainerType<?>> reg){
		ContainerType<T> contType = new ContainerType<>(cons);
		contType.setRegistryName(new ResourceLocation(MODID, id));
		reg.getRegistry().register(contType);
		return contType;
	}

	/**
	 * Creates and registers both a container type and a screen factory. Not usable on the physical server due to screen factory.
	 * @param cons Container factory
	 * @param screenFactory The screen factory to be linked to the type
	 * @param id The ID to use
	 * @param reg Registery event
	 * @param <T> Container subclass
	 */
	@OnlyIn(Dist.CLIENT)
	private static <T extends Container> void registerCon(IContainerFactory<T> cons, ScreenManager.IScreenFactory<T, ContainerScreen<T>> screenFactory, String id, RegistryEvent.Register<ContainerType<?>> reg){
		ContainerType<T> contType = registerConType(cons, id, reg);
		ScreenManager.register(contType, screenFactory);
	}
}