package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.gui.container.ESContainers;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.Da_Technomancer.essentials.Essentials.MODID;

@Mod(MODID)
public final class Essentials{

	public static final String MODID = "essentials";
	public static final String MODNAME = "Essentials";
	public static final Logger logger = LogManager.getLogger(MODNAME);

	public Essentials(IEventBus modBus, ModContainer modContainer){
		modBus.addListener(this::commonInit);
		modBus.addListener(this::clientInit);
		modBus.addListener(this::serverInit);

		ESBlocks.init(modBus);
		ESItems.init(modBus);
		ESContainers.init(modBus);

		ESConfig.init(modContainer);
	}

	private void commonInit(@SuppressWarnings("unused") FMLCommonSetupEvent e){
		//Main
		NeoForge.EVENT_BUS.register(ESEventHandlerCommon.class);
	}

	private void clientInit(@SuppressWarnings("unused") FMLClientSetupEvent e){
//		TESRRegistry.init();
		NeoForge.EVENT_BUS.register(ESEventHandlerClient.class);

		// Still needed for candle lilypad to render properly as of Forge 41.0.98
//		ItemBlockRenderTypes.setRenderLayer(hopperFilter, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(ESBlocks.candleLilyPad, RenderType.cutout());
	}

	private void serverInit(@SuppressWarnings("unused") FMLDedicatedServerSetupEvent e){
//		Keep commented until we need an event handler
//		NeoForge.EVENT_BUS.register(ESEventHandlerServer.class);
	}
}