package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EssentialsItems{

	public static final CreativeTabs TAB_ESSENTIALS = new CreativeTabs(Essentials.MODID){
		@Override
		public ItemStack getTabIconItem(){
			return new ItemStack(itemCandleLilypad, 1);
		}
	};

	public static ObsidianCuttingKit obsidianKit;
	public static ItemCandleLily itemCandleLilypad;
	public static Wrench wrench;

	/**
	 * Registers the model location for items. Item: item; Integer: the meta value to register for; ModelResourceLocation: The location to map to.
	 */
	public static final HashMap<Pair<Item, Integer>, ModelResourceLocation> toClientRegister = new HashMap<Pair<Item, Integer>, ModelResourceLocation>();
	public static final ArrayList<Item> toRegister = new ArrayList<Item>();

	/**
	 * Convenience method to add an Item to the toClientRegister map.
	 * @param item The item to register the model of
	 * @return The passed item
	 */
	public static <T extends Item> T itemAddQue(T item){
		toClientRegister.put(Pair.of(item, 0), new ModelResourceLocation(item.getRegistryName(), "inventory"));
		return item;
	}

	public static void init(){
		obsidianKit = new ObsidianCuttingKit();
		itemCandleLilypad = new ItemCandleLily();
		wrench = new Wrench();
	}

	@SideOnly(Side.CLIENT)
	public static void initModels(){
		for(Map.Entry<Pair<Item, Integer>, ModelResourceLocation> modeling : toClientRegister.entrySet()){
			ModelLoader.setCustomModelResourceLocation(modeling.getKey().getLeft(), modeling.getKey().getRight(), modeling.getValue());
		}
		toClientRegister.clear();
	}
}
