package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
import vazkii.patchouli.common.item.ItemModBook;

public class PatchouliBook extends ItemModBook{

	protected PatchouliBook(){
		super();
		String name = "guide_book";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> items){
		BookRegistry.INSTANCE.books.values().forEach((b) -> {
			if(b.getBookItem().getItem() == this && !b.isExtension && (tab == ItemGroup.TAB_SEARCH || ESIntegration.bookTabs.contains(tab))){
				ItemStack stack = new ItemStack(this);
				CompoundNBT cmp = new CompoundNBT();
				cmp.putString("patchouli:book", b.id.toString());
				stack.setTag(cmp);
				items.add(stack);
			}
		});
	}

	@Override
	public ITextComponent getName(ItemStack stack){
		Book book = getBook(stack);
		return book != null ? new TranslationTextComponent(ESIntegration.bookName) : super.getName(stack);
	}
}
