package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ESIntegration{

	public static final ArrayList<CreativeModeTab> bookTabs = new ArrayList<>();
	public static final String PATCHOULI_ID = "patchouli";
	public static final String COMPUTERCRAFT_ID = "computercraft";
	public static String bookName = "book.essentials.name";//This value should not be final, to allow other mods to change the localized name

	@Nullable
	public static Item bookItem;

	public static void initItems(){
		bookTabs.add(ESItems.TAB_ESSENTIALS);

		ModList modlist = ModList.get();
		if(modlist.isLoaded(PATCHOULI_ID)){
			PatchouliProxy.initBookItem();
		}

		if(modlist.isLoaded(COMPUTERCRAFT_ID)){
			ComputerCraftIntegration.init();
		}
	}
}
