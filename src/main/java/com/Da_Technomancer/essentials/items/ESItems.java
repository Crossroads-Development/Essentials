package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.integration.ESIntegration;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class ESItems{

	public static final CreativeModeTab TAB_ESSENTIALS = new CreativeModeTab(Essentials.MODID){
		@Override
		public ItemStack makeIcon(){
			return new ItemStack(itemCandleLilypad, 1);
		}
	};

	public static ObsidianCuttingKit obsidianKit;
	public static ItemCandleLily itemCandleLilypad;
	public static Wrench wrench;
	public static AnimalFeed animalFeed;
	public static CircuitWrench circuitWrench;
	public static LinkingTool linkingTool;

	public static final ArrayList<Item> toRegister = new ArrayList<>();

	public static void init(){
		obsidianKit = new ObsidianCuttingKit();
		itemCandleLilypad = new ItemCandleLily();
		wrench = new Wrench();
		animalFeed = new AnimalFeed();
		circuitWrench = new CircuitWrench();
		linkingTool = new LinkingTool();

		ESIntegration.initItems();
	}
}
