package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.*;
import com.Da_Technomancer.essentials.gui.container.*;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import com.Da_Technomancer.essentials.tileentities.*;
import com.mojang.datafixers.DSL;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
import static com.Da_Technomancer.essentials.blocks.EssentialsBlocks.*;

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

		EssentialsConfig.init();

		MinecraftForge.EVENT_BUS.register(this);

		EssentialsConfig.load();
	}

	private void commonInit(@SuppressWarnings("unused") FMLCommonSetupEvent e){
		//Pre
		EssentialsPackets.preInit();
		RedstoneUtil.registerCap();
		//Main
		MinecraftForge.EVENT_BUS.register(new EssentialsEventHandlerCommon());
	}

	private void clientInit(@SuppressWarnings("unused") FMLClientSetupEvent e){
		TESRRegistry.init();
		MinecraftForge.EVENT_BUS.register(new EssentialsEventHandlerClient());
		RenderingRegistry.registerEntityRenderingHandler(WitherCannon.CannonSkull.class, WitherSkullRenderer::new);
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> e){
		IForgeRegistry<Block> registry = e.getRegistry();
		EssentialsBlocks.init();
		for(Block block : toRegister){
			registry.register(block);
		}
		toRegister.clear();
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> e){
		IForgeRegistry<Item> registry = e.getRegistry();
		EssentialsItems.init();
		for(Item item : EssentialsItems.toRegister){
			registry.register(item);
		}
		EssentialsItems.toRegister.clear();
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerEnts(RegistryEvent.Register<EntityType<?>> e){
		IForgeRegistry<EntityType<?>> registry = e.getRegistry();
		registry.register(EntityType.Builder.create(WitherCannon.CannonSkull::new, EntityClassification.MISC).setShouldReceiveVelocityUpdates(true).size(0.3125F, 0.3125F).immuneToFire().setUpdateInterval(4).setTrackingRange(4).setCustomClientFactory((FMLPlayMessages.SpawnEntity s, World w) -> new WitherCannon.CannonSkull(WitherCannon.ENT_TYPE, w)).build("cannon_skull").setRegistryName(Essentials.MODID, "cannon_skull"));
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> e){
		IForgeRegistry<TileEntityType<?>> reg = e.getRegistry();
		registerTE(BrazierTileEntity::new, "brazier", reg, brazier);
		registerTE(SlottedChestTileEntity::new, "slotted_chest", reg, slottedChest);
		registerTE(SortingHopperTileEntity::new, "sorting_hopper", reg, sortingHopper);
		registerTE(SpeedHopperTileEntity::new, "speed_hopper", reg, speedHopper);
		registerTE(ItemShifterTileEntity::new, "item_shifter", reg, itemShifter);
		registerTE(FluidShifterTileEntity::new, "fluid_shifter", reg, fluidShifter);
		registerTE(HopperFilterTileEntity::new, "hopper_filter", reg, hopperFilter);
		registerTE(BasicItemSplitterTileEntity::new, "basic_item_splitter", reg, basicItemSplitter);
		registerTE(ItemSplitterTileEntity::new, "item_splitter", reg, itemSplitter);
		registerTE(CircuitTileEntity::new, "circuit", reg, andCircuit, orCircuit, interfaceCircuit, notCircuit, xorCircuit, maxCircuit, minCircuit, sumCircuit, difCircuit, prodCircuit, quotCircuit, powCircuit, invCircuit, cosCircuit, sinCircuit, tanCircuit, asinCircuit, acosCircuit, atanCircuit);
		registerTE(ConstantCircuitTileEntity::new, "cons_circuit", reg, consCircuit);
		registerTE(WireTileEntity::new, "wire", reg, wireCircuit);
		registerTE(WireTileEntity::new, "wire_junction", reg, wireJunctionCircuit);
		registerTE(AutoCrafterTileEntity::new, "auto_crafter", reg, autoCrafter);
	}

	private static void registerTE(Supplier<? extends TileEntity> cons, String id, IForgeRegistry<TileEntityType<?>> reg, Block... blocks){
		TileEntityType teType = TileEntityType.Builder.create(cons, blocks).build(DSL.nilType());
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
		registerCon(AutoCrafterContainer::new, AutoCrafterScreen::new, "auto_crafter", e);
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
		registerConType(AutoCrafterContainer::new, "auto_crafter", e);
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
		ScreenManager.registerFactory(contType, screenFactory);
	}
}