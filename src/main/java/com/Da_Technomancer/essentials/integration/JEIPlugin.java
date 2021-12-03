package com.Da_Technomancer.essentials.integration;

//@JeiPlugin
//@SuppressWarnings("unused")
//public class JEIPlugin implements IModPlugin{
//
//	@Override
//	public ResourceLocation getPluginUid(){
//		return new ResourceLocation(Essentials.MODID, "jei_plugin");
//	}
//
//	@Override
//	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration){
//		registration.addRecipeTransferHandler(new AutoCrafterTransfer(registration.getTransferHelper()), new ResourceLocation("crafting"));
//	}
//
//	private static class AutoCrafterTransfer implements IRecipeTransferHandler<AutoCrafterContainer, CraftingRecipe>{
//
//		private final IRecipeTransferHandlerHelper helper;
//
//		private AutoCrafterTransfer(IRecipeTransferHandlerHelper helper){
//			this.helper = helper;
//		}
//
//		@Override
//		public Class<AutoCrafterContainer> getContainerClass(){
//			return AutoCrafterContainer.class;
//		}
//
//        @Override
//        public Class<CraftingRecipe> getRecipeClass()
//        {
//            return CraftingRecipe.class;
//        }
//
//        @Nullable
//		@Override
//		public IRecipeTransferError transferRecipe(AutoCrafterContainer c, CraftingRecipe recipe, IRecipeLayout iRecipeLayout, Player playerEntity, boolean maxTransfer, boolean doTransfer){
//			try{
//				if(doTransfer){
//					CraftingContainer craftInv = new CraftingContainer(new AbstractContainerMenu(null, 0){
//						@Override
//						public boolean stillValid(Player playerIn){
//							return false;
//						}
//					}, 3, 3);
//
//
//					for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : iRecipeLayout.getItemStacks().getGuiIngredients().entrySet()){
//						if(entry.getKey() == 0){
//							continue;
//						}
//
//						ItemStack ingr = entry.getValue().getDisplayedIngredient();
//						craftInv.setItem(entry.getKey() - 1, ingr == null ? ItemStack.EMPTY : ingr);
//					}
//
//					Optional<CraftingRecipe> recipeOptional = Minecraft.getInstance().getConnection().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftInv, playerEntity.level);
//
//					if(recipeOptional.isPresent() && c.te != null){
//						c.te.recipe = recipeOptional.orElse(null).getId();
//						CompoundTag nbt = new CompoundTag();
//						nbt.putString("recipe", c.te.recipe.toString());
//						EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, c.te.getBlockPos()));
//					}
//				}
//			}catch(Exception e){
//				return helper.createUserErrorWithTooltip(new TranslatableComponent("tt.essentials.jei.recipe_transfer.fail"));
//			}
//			return null;
//		}
//	}
//}


public class JEIPlugin{
	//TODO switch back when possible
}