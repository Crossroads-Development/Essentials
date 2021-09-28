package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.gui.*;
import com.Da_Technomancer.essentials.gui.container.*;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.render.CannonSkullRenderer;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import com.Da_Technomancer.essentials.tileentities.*;
import com.Da_Technomancer.essentials.tileentities.redstone.*;
import com.mojang.datafixers.DSL;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.FMLPlayMessages;
import net.minecraftforge.fmllegacy.network.IContainerFactory;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		//Main
		MinecraftForge.EVENT_BUS.register(new ESEventHandlerCommon());
	}

	private void clientInit(@SuppressWarnings("unused") FMLClientSetupEvent e){
		TESRRegistry.init();
		MinecraftForge.EVENT_BUS.register(new ESEventHandlerClient());
		ItemBlockRenderTypes.setRenderLayer(hopperFilter, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(candleLilyPad, RenderType.cutout());
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent e){
		e.register(IRedstoneHandler.class);
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerModels(ModelRegistryEvent e){
		EntityRenderers.register(WitherCannon.ENT_TYPE, CannonSkullRenderer::new);
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
		registry.register(EntityType.Builder.of(WitherCannon.CannonSkull::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).sized(0.3125F, 0.3125F).fireImmune().setUpdateInterval(4).setTrackingRange(4).setCustomClientFactory((FMLPlayMessages.SpawnEntity s, Level w) -> new WitherCannon.CannonSkull(WitherCannon.ENT_TYPE, w)).build("cannon_skull").setRegistryName(Essentials.MODID, "cannon_skull"));
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
	}

	private static void registerTE(BlockEntityType.BlockEntitySupplier<? extends BlockEntity> cons, String id, IForgeRegistry<BlockEntityType<?>> reg, Block... blocks){
		BlockEntityType<?> teType = BlockEntityType.Builder.of(cons, blocks).build(DSL.emptyPartType());
		teType.setRegistryName(new ResourceLocation(MODID, id));
		reg.register(teType);
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	@OnlyIn(Dist.CLIENT)
	public static void registerContainers(RegistryEvent.Register<MenuType<?>> e){
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
	public static void registerContainerTypes(RegistryEvent.Register<MenuType<?>> e){
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
	private static <T extends AbstractContainerMenu> MenuType<T> registerConType(IContainerFactory<T> cons, String id, RegistryEvent.Register<MenuType<?>> reg){
		MenuType<T> contType = new MenuType<>(cons);
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
	private static <T extends AbstractContainerMenu> void registerCon(IContainerFactory<T> cons, MenuScreens.ScreenConstructor<T, AbstractContainerScreen<T>> screenFactory, String id, RegistryEvent.Register<MenuType<?>> reg){
		MenuType<T> contType = registerConType(cons, id, reg);
		MenuScreens.register(contType, screenFactory);
	}
}