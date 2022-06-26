package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.Da_Technomancer.essentials.Essentials.MODID;
import static com.Da_Technomancer.essentials.blocks.ESBlocks.candleLilyPad;
import static com.Da_Technomancer.essentials.blocks.ESBlocks.hopperFilter;

@Mod(MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Essentials{

	public static final String MODID = "essentials";
	public static final String MODNAME = "Essentials";
	public static final Logger logger = LogManager.getLogger(MODNAME);

	private final IEventBus MOD_EVENT_BUS;

	public Essentials(){
		MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
		MOD_EVENT_BUS.addListener(this::commonInit);
		MOD_EVENT_BUS.addListener(this::clientInit);
		MOD_EVENT_BUS.addListener(this::serverInit);

//		MinecraftForge.EVENT_BUS.register(this);

		ESConfig.init();
		ESConfig.load();
	}

	private void commonInit(@SuppressWarnings("unused") FMLCommonSetupEvent e){
		//Pre
		EssentialsPackets.preInit();
		//Main
		MinecraftForge.EVENT_BUS.register(ESEventHandlerCommon.class);
//		MOD_EVENT_BUS.register(ESEventHandlerCommon.ESModEventsCommon.class);
	}

	private void clientInit(@SuppressWarnings("unused") FMLClientSetupEvent e){
		TESRRegistry.init();
		MinecraftForge.EVENT_BUS.register(ESEventHandlerClient.class);
//		MOD_EVENT_BUS.register(ESEventHandlerClient.ESModEventsClient.class);

		ItemBlockRenderTypes.setRenderLayer(hopperFilter, RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(candleLilyPad, RenderType.cutout());
	}

	private void serverInit(@SuppressWarnings("unused") FMLDedicatedServerSetupEvent e){
		MinecraftForge.EVENT_BUS.register(ESEventHandlerServer.class);
//		MOD_EVENT_BUS.register(ESEventHandlerServer.ESModEventsServer.class);
	}
}