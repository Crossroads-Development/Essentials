package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.integration.ESIntegration;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ESItems{

	public static CreativeModeTab ESSENTIALS_TAB;

	public static ObsidianCuttingKit obsidianKit;
	public static ItemCandleLily itemCandleLilypad;
	public static Wrench wrench;
	public static AnimalFeed animalFeed;
	public static CircuitWrench circuitWrench;
	public static LinkingTool linkingTool;

	private static final HashMap<String, Item> toRegister = new HashMap<>();
	public static final ArrayList<Supplier<ItemStack[]>> creativeTabItems = new ArrayList<>();

	/**
	 * Queues up an item to be registered and added to the creative tab
	 * @param regName Item registry name (without essentials: prefix)
	 * @param item Item
	 * @param creativeItems All itemstacks to be registered to the creative tab. Null for no creative tab items.
	 * @return The item
	 * @param <T> Item class
	 */
	public static <T extends Item> T queueForRegister(String regName, T item, Supplier<ItemStack[]> creativeItems){
		toRegister.put(regName, item);
		if(creativeItems != null){
			creativeTabItems.add(creativeItems);
		}
		return item;
	}

	/**
	 * Queues up an item to be registered and added to the creative tab
	 * @param regName Item registry name (without essentials: prefix)
	 * @param item Item
	 * @return The item
	 * @param <T> Item class
	 */
	public static <T extends Item> T queueForRegister(String regName, T item){
		return queueForRegister(regName, item, () -> new ItemStack[] {new ItemStack(item)});
	}

	public static Item.Properties baseItemProperties(){
		return new Item.Properties();
	}

	public static void init(RegisterEvent.RegisterHelper<net.minecraft.world.item.Item> helper){
		itemCandleLilypad = new ItemCandleLily();
		wrench = new Wrench();
		circuitWrench = new CircuitWrench();
		linkingTool = new LinkingTool();
		obsidianKit = new ObsidianCuttingKit();
		animalFeed = new AnimalFeed();

		ESIntegration.initItems();

		for(Map.Entry<String, Item> item : toRegister.entrySet()){
			helper.register(item.getKey(), item.getValue());
		}
		toRegister.clear();
	}
}
