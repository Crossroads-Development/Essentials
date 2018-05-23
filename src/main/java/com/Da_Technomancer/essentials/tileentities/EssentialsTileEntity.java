package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EssentialsTileEntity{

	public static void init(){
		registerOld(BrazierTileEntity.class, "brazier");
		registerOld(SlottedChestTileEntity.class, "slottedChest");
		registerOld(SortingHopperTileEntity.class, "sortingHopper");
		register(PortExtenderTileEntity.class, "port_extender", false);
	}

	/**
	 * @deprecated A single character was wrong. Changing it will destroy all Crossroads stuff in existing worlds. All new tile entities should use the other method. 
	 */
	@Deprecated
	private static void registerOld(Class<? extends TileEntity> locat, String ID){
		GameRegistry.registerTileEntity(locat, "crossroads" + '_' + ID);
	}

	/**
	 * @param clazz The class of the TileEntity being registered. 
	 * @param ID Should be lower-case.
	 */
	private static void register(Class<? extends TileEntity> clazz, String ID, boolean newTE){
		GameRegistry.registerTileEntity(clazz, (newTE ? Essentials.MODID : "crossroads") + ':' + ID);
	}
}
