package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.gui.EssentialsGuiHandler;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.items.crafting.EssentialsCrafting;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import com.Da_Technomancer.essentials.tileentities.*;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static com.Da_Technomancer.essentials.Essentials.MODID;

@Mod(MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Essentials{

	public static final String MODID = "essentials";
	public static final String MODNAME = "Essentials";
	public static final Logger logger = LogManager.getLogger(MODNAME);


	public Essentials(){
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);

		EssentialsConfig.init();
		MinecraftForge.EVENT_BUS.register(this);

		EssentialsConfig.load();
	}

	private void commonInit(FMLCommonSetupEvent e){
		//Pre
		EssentialsPackets.preInit();
		//Main
		MinecraftForge.EVENT_BUS.register(new EssentialsEventHandlerCommon());
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, EssentialsGuiHandler::new);
		EssentialsCrafting.init();
	}

	private void clientInit(FMLClientSetupEvent e){
		TESRRegistry.init();
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> e){
		IForgeRegistry<Block> registry = e.getRegistry();
		EssentialsBlocks.init();
		for(Block block : EssentialsBlocks.toRegister){
			registry.register(block);
		}
		EssentialsBlocks.toRegister.clear();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> e){
		IForgeRegistry<Item> registry = e.getRegistry();
		EssentialsItems.init();
		for(Item item : EssentialsItems.toRegister){
			registry.register(item);
		}
		EssentialsItems.toRegister.clear();
	}

	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> e){
		IForgeRegistry<TileEntityType<?>> reg = e.getRegistry();
		register(BrazierTileEntity::new, "brazier", reg);
		register(SlottedChestTileEntity::new, "slotted_chest", reg);
		register(SortingHopperTileEntity::new, "sorting_hopper", reg);
		register(SpeedHopperTileEntity::new, "speed_hopper", reg);
		register(ItemShifterTileEntity::new, "item_shifter", reg);
		register(HopperFilterTileEntity::new, "hopper_filter", reg);
		register(BasicItemSplitterTileEntity::new, "basic_item_splitter", reg);
		register(ItemSplitterTileEntity::new, "item_splitter", reg);
		register(FluidShifterTileEntity::new, "fluid_splitter", reg);
	}

	private static void register(Supplier<? extends TileEntity> cons, String id, IForgeRegistry<TileEntityType<?>> reg){
		TileEntityType teType;

		Type type = null;
		/*
		try{
			//1631 probably the wrong number
			type = DataFixesManager.getDataFixer().getSchema(DataFixUtils.makeKey(1631)).getChoiceType(TypeReferences.BLOCK_ENTITY, id);
		}catch (IllegalArgumentException ex){
			if(SharedConstants.developmentMode){
				throw ex;
			}
			Essentials.logger.warn("No data fixer registered for block entity {}", id);
			return;
		}
		*/
		teType = TileEntityType.Builder.create(cons).build(type);
		teType.setRegistryName(new ResourceLocation(MODID, id));
		reg.register(teType);
	}

	//TODO register recipes
}