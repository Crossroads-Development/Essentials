package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class EssentialsItems{

	public static final ItemGroup TAB_ESSENTIALS = new ItemGroup(Essentials.MODID){
		@Override
		public ItemStack createIcon(){
			return new ItemStack(itemCandleLilypad, 1);
		}
	};

	public static ObsidianCuttingKit obsidianKit;
	public static ItemCandleLily itemCandleLilypad;
	public static Wrench wrench;
	public static AnimalFeed animalFeed;
	public static CircuitWrench circuitWrench;

	public static final ArrayList<Item> toRegister = new ArrayList<>();

	public static void init(){
		obsidianKit = new ObsidianCuttingKit();
		itemCandleLilypad = new ItemCandleLily();
		wrench = new Wrench();
		animalFeed = new AnimalFeed();
//		circuitWrench = new CircuitWrench();
	}
}
