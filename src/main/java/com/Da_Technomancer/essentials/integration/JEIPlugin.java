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
import net.minecraft.entity.player.Player;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

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

	private static class AutoCrafterTransfer implements IRecipeTransferHandler<AutoCrafterContainer>{

		private final IRecipeTransferHandlerHelper helper;

		private AutoCrafterTransfer(IRecipeTransferHandlerHelper helper){
			this.helper = helper;
		}

		@Override
		public Class<AutoCrafterContainer> getContainerClass(){
			return AutoCrafterContainer.class;
		}

		@Nullable
		@Override
		public IRecipeTransferError transferRecipe(AutoCrafterContainer c, IRecipeLayout iRecipeLayout, Player playerEntity, boolean maxTransfer, boolean doTransfer){
			try{
				if(doTransfer){
					CraftingInventory craftInv = new CraftingInventory(new Container(null, 0){
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

					Optional<ICraftingRecipe> recipeOptional = Minecraft.getInstance().getConnection().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftInv, playerEntity.level);

					if(recipeOptional.isPresent() && c.te != null){
						c.te.recipe = recipeOptional.orElse(null).getId();
						CompoundNBT nbt = new CompoundNBT();
						nbt.putString("recipe", c.te.recipe.toString());
						EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, c.te.getBlockPos()));
					}
				}
			}catch(Exception e){
				return helper.createUserErrorWithTooltip("Failed to transfer recipe");
			}
			return null;
		}
	}
}
