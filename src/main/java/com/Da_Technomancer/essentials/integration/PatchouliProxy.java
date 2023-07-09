package com.Da_Technomancer.essentials.integration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;

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
			if(book.getBookItem().getItem() == ESIntegration.bookItem && !book.isExtension){
				ItemStack stack = new ItemStack(ESIntegration.bookItem);
				CompoundTag cmp = new CompoundTag();
				cmp.putString("patchouli:book", book.id.toString());
				stack.setTag(cmp);
				return stack;
			}
		};
		return ItemStack.EMPTY;
	}
}
