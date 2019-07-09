package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.ItemShifterScreen;
import com.Da_Technomancer.essentials.gui.SlottedChestScreen;
import com.Da_Technomancer.essentials.gui.container.ItemShifterContainer;
import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import com.Da_Technomancer.essentials.tileentities.*;
import com.mojang.datafixers.DSL;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

	private void commonInit(FMLCommonSetupEvent e){
		//Pre
		EssentialsPackets.preInit();
		RedstoneUtil.registerCap();
		//Main
		MinecraftForge.EVENT_BUS.register(new EssentialsEventHandlerCommon());
	}

	private void clientInit(FMLClientSetupEvent e){
		TESRRegistry.init();
		MinecraftForge.EVENT_BUS.register(new EssentialsEventHandlerClient());
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> e){
		IForgeRegistry<Block> registry = e.getRegistry();
		init();
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
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> e){
		IForgeRegistry<TileEntityType<?>> reg = e.getRegistry();
		registerTE(BrazierTileEntity::new, "brazier", reg, brazier);
		registerTE(SlottedChestTileEntity::new, "slotted_chest", reg, slottedChest);
		registerTE(SortingHopperTileEntity::new, "sorting_hopper", reg, sortingHopper);
		registerTE(SpeedHopperTileEntity::new, "speed_hopper", reg, speedHopper);
		registerTE(ItemShifterTileEntity::new, "item_shifter", reg, itemShifter);
		registerTE(HopperFilterTileEntity::new, "hopper_filter", reg, hopperFilter);
		registerTE(BasicItemSplitterTileEntity::new, "basic_item_splitter", reg, basicItemSplitter);
		registerTE(ItemSplitterTileEntity::new, "item_splitter", reg, itemSplitter);
//		registerTE(FluidShifterTileEntity::new, "fluid_splitter", reg, EssentialsBlocks.fluidShifter);
		registerTE(CircuitTileEntity::new, "circuit", reg, andCircuit, consCircuit, interfaceCircuit, notCircuit, xorCircuit);
		registerTE(WireTileEntity::new, "wire", reg, wireCircuit);
		registerTE(WireTileEntity::new, "wire_junction", reg, wireJunctionCircuit);
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
		registerCon(SlottedChestContainer::new, SlottedChestScreen::new, "slotted_chest", e);
	}

	@OnlyIn(Dist.CLIENT)
	private static <T extends Container> void registerCon(IContainerFactory<T> cons, ScreenManager.IScreenFactory<T, ContainerScreen<T>> screenFactory, String id, RegistryEvent.Register<ContainerType<?>> reg){
		ContainerType<T> contType = new ContainerType<>(cons);
		contType.setRegistryName(new ResourceLocation(MODID, id));
		reg.getRegistry().register(contType);
		ScreenManager.registerFactory(contType, screenFactory);
	}
}