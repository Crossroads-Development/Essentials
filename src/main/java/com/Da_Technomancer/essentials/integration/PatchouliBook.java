package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.item.ItemModBook;

public class PatchouliBook extends ItemModBook{

	protected PatchouliBook(){
		super();
		String name = "guide_book";
		ESItems.queueForRegister(name, this, () -> new ItemStack[] {PatchouliProxy.getBookStack()});
	}

	@Override
	public Component getName(ItemStack stack){
		Book book = getBook(stack);
		return book != null ? Component.translatable(ESIntegration.bookName) : super.getName(stack);
	}
}

//Fallback implementation for if Patchouli is not available
//public class PatchouliBook extends Item{
//
//	protected PatchouliBook(){
//		super(new Item.Properties().stacksTo(1).tab(ESItems.TAB_ESSENTIALS));
//		String name = "guide_book";
//		setRegistryName(name);
//		ESItems.toRegister.put(name, this);
//
//		Essentials.logger.info("Attempted to initialize Patchouli integration, but it has been disabled in code");
//		Essentials.logger.info("Notify the mod author that Patchouli integration should be re-enabled");
//	}
//}