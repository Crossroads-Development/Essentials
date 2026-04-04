package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vazkii.patchouli.common.item.PatchouliDataComponents;

public class PatchouliProxy{

	/*
	 * This class will crash if anything inside is called when Patchouli is not installed, but can be referenced as long as it isn't called
	 */

	public static void initBookItem(){
		// Intended to allow Crossroads to make changes to the book in a more extensive way than normally allowed
		ESIntegration.bookItem = new PatchouliBook();
	}

	public static ItemStack getBookStack(){
		if(ESIntegration.bookItem == null){
			return new ItemStack(Items.BOOK);//Failsafe so we don't have an empty stack
		}
		ItemStack stack = new ItemStack(ESIntegration.bookItem);
		stack.set(PatchouliDataComponents.BOOK, ResourceLocation.fromNamespaceAndPath(Essentials.MODID, "manual"));
		return stack;
	}
}
