package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.SendNBTToServer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

@JeiPlugin
@SuppressWarnings("unused")
public class JEIPlugin implements IModPlugin{

	@Override
	public ResourceLocation getPluginUid(){
		return new ResourceLocation(Essentials.MODID, "jei_plugin");
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration){
		registration.addRecipeTransferHandler(new AutoCrafterTransfer(registration.getTransferHelper()), new ResourceLocation("crafting"));
	}

	private static class AutoCrafterTransfer implements IRecipeTransferHandler<AutoCrafterContainer, CraftingRecipe>{

		private final IRecipeTransferHandlerHelper helper;

		private AutoCrafterTransfer(IRecipeTransferHandlerHelper helper){
			this.helper = helper;
		}

		@Override
		public Class<AutoCrafterContainer> getContainerClass(){
			return AutoCrafterContainer.class;
		}

        @Override
        public Class<CraftingRecipe> getRecipeClass()
        {
            return CraftingRecipe.class;
        }

        @Nullable
		@Override
		public IRecipeTransferError transferRecipe(AutoCrafterContainer c, CraftingRecipe recipe, IRecipeLayout iRecipeLayout, Player playerEntity, boolean maxTransfer, boolean doTransfer){
			try{
				if(doTransfer){
					CraftingContainer craftInv = new CraftingContainer(new AbstractContainerMenu(null, 0){
						@Override
						public boolean stillValid(Player playerIn){
							return false;
						}
					}, 3, 3);


					for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : iRecipeLayout.getItemStacks().getGuiIngredients().entrySet()){
						if(entry.getKey() == 0){
							continue;
						}

						ItemStack ingr = entry.getValue().getDisplayedIngredient();
						craftInv.setItem(entry.getKey() - 1, ingr == null ? ItemStack.EMPTY : ingr);
					}

					Optional<CraftingRecipe> recipeOptional = Minecraft.getInstance().getConnection().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftInv, playerEntity.level);

					if(recipeOptional.isPresent() && c.te != null){
						c.te.recipe = recipeOptional.orElse(null).getId();
						CompoundTag nbt = new CompoundTag();
						nbt.putString("recipe", c.te.recipe.toString());
						EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, c.te.getBlockPos()));
					}
				}
			}catch(Exception e){
				return helper.createUserErrorWithTooltip(new TranslatableComponent("tt.essentials.jei.recipe_transfer.fail"));
			}
			return null;
		}
	}
}
