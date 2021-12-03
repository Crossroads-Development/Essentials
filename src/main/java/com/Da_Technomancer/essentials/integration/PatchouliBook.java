package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.world.item.Item;

//public class PatchouliBook extends ItemModBook{
//
//	protected PatchouliBook(){
//		super();
//		String name = "guide_book";
//		setRegistryName(name);
//		ESItems.toRegister.add(this);
//	}
//
//	@Override
//	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items){
//		BookRegistry.INSTANCE.books.values().forEach((b) -> {
//			if(b.getBookItem().getItem() == this && !b.isExtension && (tab == CreativeModeTab.TAB_SEARCH || ESIntegration.bookTabs.contains(tab))){
//				ItemStack stack = new ItemStack(this);
//				CompoundTag cmp = new CompoundTag();
//				cmp.putString("patchouli:book", b.id.toString());
//				stack.setTag(cmp);
//				items.add(stack);
//			}
//		});
//	}
//
//	@Override
//	public Component getName(ItemStack stack){
//		Book book = getBook(stack);
//		return book != null ? new TranslatableComponent(ESIntegration.bookName) : super.getName(stack);
//	}
//}

public class PatchouliBook extends Item{

	protected PatchouliBook(){
		super(new Item.Properties().stacksTo(1).tab(ESItems.TAB_ESSENTIALS));
		String name = "guide_book";
		setRegistryName(name);
		ESItems.toRegister.add(this);

		//TODO switch back to other implementation when possible
		Essentials.logger.info("Attempted to initialize Patchouli integration, but it has been disabled in code");
		Essentials.logger.info("Notify the mod author that Patchouli integration should be re-enabled");
	}
}