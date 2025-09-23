package com.Da_Technomancer.essentials.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
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
		for(Book book : BookRegistry.INSTANCE.books.values()){
			if(book.getBookItem().getItem() == ESIntegration.bookItem && !book.isExternal){
				ItemStack stack = new ItemStack(ESIntegration.bookItem);
				ResourceLocation id = book.id;
				stack.set(PatchouliDataComponents.BOOK, id);
				return stack;
			}
		};
		return ItemStack.EMPTY;
	}
}
