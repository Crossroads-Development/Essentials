package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.Da_Technomancer.essentials.api.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.api.packets.SendNBTToServer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;

import javax.annotation.Nullable;
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
		registration.addRecipeTransferHandler(new AutoCrafterTransfer(registration.getTransferHelper()), RecipeTypes.CRAFTING);
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
		public Optional<MenuType<AutoCrafterContainer>> getMenuType(){
			return Optional.ofNullable(AutoCrafterContainer.TYPE);
		}

		@Override
		public RecipeType<CraftingRecipe> getRecipeType(){
			return RecipeTypes.CRAFTING;
		}

		@Nullable
		@Override
		public IRecipeTransferError transferRecipe(AutoCrafterContainer c, CraftingRecipe recipe, IRecipeSlotsView recipeView, Player playerEntity, boolean maxTransfer, boolean doTransfer){
			try{
				if(doTransfer){
					if(c.te != null){
						c.te.recipe = recipe.getId();
						CompoundTag nbt = new CompoundTag();
						nbt.putString("recipe", c.te.recipe.toString());
						EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, c.te.getBlockPos()));
					}
				}
			}catch(Exception e){
				return helper.createUserErrorWithTooltip(Component.translatable("tt.essentials.jei.recipe_transfer.fail"));
			}
			return null;
		}
	}
}


//public class JEIPlugin{
//	//Fake implementation for initial ports without JEI
//}