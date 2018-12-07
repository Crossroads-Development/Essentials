package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class EssentialsTileEntity{

	public static void init(){
		register(BrazierTileEntity.class, "brazier");
		register(SlottedChestTileEntity.class, "slotted_chest");
		register(SortingHopperTileEntity.class, "sorting_hopper");
		register(SpeedHopperTileEntity.class, "speed_hopper");
		register(ItemShifterTileEntity.class, "item_shifter");
		register(PortExtenderTileEntity.class, "port_extender");
		register(BasicItemSplitterTileEntity.class, "basic_item_splitter");
		register(ItemSplitterTileEntity.class, "item_splitter");
	}

	/**
	 * @param clazz The class of the TileEntity being registered. 
	 * @param ID Should be lower-case.
	 */
	private static void register(Class<? extends TileEntity> clazz, String ID){
		GameRegistry.registerTileEntity(clazz, Essentials.MODID + ':' + ID);
		//TODO switch over once the above method is removed GameRegistry.registerTileEntity(clazz, new ResourceLocation(Essentials.MODID, ID));
	}
}
