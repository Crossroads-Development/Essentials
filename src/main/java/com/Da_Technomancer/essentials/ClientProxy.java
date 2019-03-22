package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy{
	
	@Override
	protected void preInit(FMLPreInitializationEvent e){
		super.preInit(e);
	}

	@Override
	protected void init(FMLInitializationEvent e){
		super.init(e);
		TESRRegistry.init();
	}

	@Override
	protected void postInit(FMLPostInitializationEvent e){
		super.postInit(e);
	}
	
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent e){
		EssentialsItems.initModels();
	}
}
