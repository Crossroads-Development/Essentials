package com.Da_Technomancer.essentials.integration;

//TODO commented out until JEI updates
public class JEIPlugin{

}

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
//	private static class AutoCrafterTransfer implements IRecipeTransferHandler<AutoCrafterContainer>{
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
//		@Nullable
//		@Override
//		public IRecipeTransferError transferRecipe(AutoCrafterContainer c, IRecipeLayout iRecipeLayout, PlayerEntity playerEntity, boolean maxTransfer, boolean doTransfer){
//			try{
//				if(doTransfer){
//					CraftingInventory craftInv = new CraftingInventory(new Container(null, 0){
//						@Override
//						public boolean canInteractWith(PlayerEntity playerIn){
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
//						craftInv.setInventorySlotContents(entry.getKey() - 1, ingr == null ? ItemStack.EMPTY : ingr);
//					}
//
//					Optional<ICraftingRecipe> recipeOptional = Minecraft.getInstance().getConnection().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftInv, playerEntity.world);
//
//					if(recipeOptional.isPresent() && c.te != null){
//						c.te.recipe = recipeOptional.orElse(null).getId();
//						CompoundNBT nbt = new CompoundNBT();
//						nbt.putString("recipe", c.te.recipe.toString());
//						EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, c.te.getPos()));
//					}
//				}
//			}catch(Exception e){
//				return helper.createUserErrorWithTooltip("Failed to transfer recipe");
//			}
//			return null;
//		}
//	}
//}
