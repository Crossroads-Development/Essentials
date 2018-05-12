package com.Da_Technomancer.essentials;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Essentials.MODID, name = Essentials.MODNAME, version = Essentials.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "required-after:forge@[13.20.0.2271,]; after:jei")
public final class Essentials{

	public static final String MODID = "essentials";
	public static final String MODNAME = "Essentials";
	public static final String VERSION = "gradVERSION";

	public static Logger logger;

	@SidedProxy(clientSide = "com.Da_Technomancer.essentials.ClientProxy", serverSide = "com.Da_Technomancer.essentials.ServerProxy")
	public static CommonProxy proxy;

	@Mod.Instance
	public static Essentials instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e){
		logger = e.getModLog();
		proxy.preInit(e);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent e){
		proxy.init(e);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent e){
		proxy.postInit(e);
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent e){
		//For singleplayer.
		EssentialsConfig.syncPropNBT = EssentialsConfig.nbtToSyncConfig();
	}
}