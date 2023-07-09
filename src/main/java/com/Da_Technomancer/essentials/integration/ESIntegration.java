package com.Da_Technomancer.essentials.integration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;

public class ESIntegration{

	public static final String PATCHOULI_ID = "patchouli";
	public static final String COMPUTERCRAFT_ID = "computercraft";
	public static String bookName = "book.essentials.name";//This value should not be final, to allow other mods to change the localized name

	@Nullable
	public static Item bookItem;

	public static ItemStack getBookStack(){
		if(bookItem != null){
			return PatchouliProxy.getBookStack();
		}
		return ItemStack.EMPTY;
	}

	public static void initItems(){
		ModList modlist = ModList.get();
		if(modlist.isLoaded(PATCHOULI_ID)){
			PatchouliProxy.initBookItem();
		}

		if(modlist.isLoaded(COMPUTERCRAFT_ID)){
			ComputerCraftIntegration.init();
		}
	}
}
