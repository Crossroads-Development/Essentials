package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ESIntegration{

	public static final ArrayList<ItemGroup> bookTabs = new ArrayList<>();
	public static final String PATCHOULI_ID = "patchouli";
	public static String bookName = "book.essentials.name";

	@Nullable
	public static Item bookItem;

	public static void initItems(){
		bookTabs.add(ESItems.TAB_ESSENTIALS);

		if(ModList.get().isLoaded(PATCHOULI_ID)){
			PatchouliProxy.initBookItem();
		}
	}
}
