package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EssentialsTileEntity{

	public static void init(){
		register(BrazierTileEntity.class, "brazier");
		register(SlottedChestTileEntity.class, "slotted_chest");
		register(SortingHopperTileEntity.class, "sorting_hopper");
		register(ItemChutePortTileEntity.class, "item_chute_port");
		register(PortExtenderTileEntity.class, "port_extender");
	}

	/**
	 * @param clazz The class of the TileEntity being registered. 
	 * @param ID Should be lower-case.
	 */
	private static void register(Class<? extends TileEntity> clazz, String ID){
		GameRegistry.registerTileEntity(clazz, Essentials.MODID + ':' + ID);
	}
}
