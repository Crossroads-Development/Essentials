package com.Da_Technomancer.essentials.integration;

//TODO

//Non-implementation until Patchouli is updated

public class PatchouliBook{

}
/*
public class PatchouliBook extends ItemModBook{

	protected PatchouliBook(){
		super();
		String name = "guide_book";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items){
		BookRegistry.INSTANCE.books.values().forEach((b) -> {
			if(b.getBookItem().getItem() == this && !b.isExtension && (tab == CreativeModeTab.TAB_SEARCH || ESIntegration.bookTabs.contains(tab))){
				ItemStack stack = new ItemStack(this);
				CompoundTag cmp = new CompoundTag();
				cmp.putString("patchouli:book", b.id.toString());
				stack.setTag(cmp);
				items.add(stack);
			}
		});
	}

	@Override
	public Component getName(ItemStack stack){
		Book book = getBook(stack);
		return book != null ? new TranslatableComponent(ESIntegration.bookName) : super.getName(stack);
	}
}
*/
