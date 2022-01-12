package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.AutoCrafterScreen;
import com.Da_Technomancer.essentials.tileentities.AutoCrafterTileEntity;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;

@ObjectHolder(Essentials.MODID)
public class AutoCrafterContainer extends RecipeBookMenu<CraftingContainer>{

	@ObjectHolder("auto_crafter")
	private static MenuType<AutoCrafterContainer> TYPE = null;

	@Nullable
	public final AutoCrafterTileEntity te;
	private final Container inv;
	private final Inventory playerInv;

	public AutoCrafterContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		this(TYPE, id, playerInventory, data);
	}

	protected AutoCrafterContainer(MenuType<? extends AutoCrafterContainer> type, int id, Inventory playerInventory, FriendlyByteBuf data){
		this(type, id, playerInventory, new SimpleContainer(19), data.readBlockPos());
	}

	public AutoCrafterContainer(int id, Inventory playerInventory, Container inv, BlockPos pos){
		this(TYPE, id, playerInventory, inv, pos);
	}

	protected AutoCrafterContainer(MenuType<? extends AutoCrafterContainer> type, int id, Inventory playerInventory, Container inv, BlockPos pos){
		super(type, id);
		playerInv = playerInventory;
		BlockEntity getTe = playerInventory.player.level.getBlockEntity(pos);
		if(getTe instanceof AutoCrafterTileEntity){
			te = (AutoCrafterTileEntity) getTe;
		}else{
			//This should never happen, but we need to account for the possibility of time delay/network weirdness
			te = null;
		}

		this.inv = inv;

		//Input slots
		for(int i = 0; i < 9; i++){
			addSlot(new Slot(inv, i, 8 + i * 18, 73){
				@Override
				public boolean mayPlace(ItemStack stack){
					if(te == null){
						return false;
					}
					int freeSlots = te.getLegalSlots(stack.getItem(), inv, AutoCrafterContainer.this) - te.getUsedSlots(stack.getItem(), inv);
					return freeSlots > 0 || freeSlots == 0 && BlockUtil.sameItem(getItem(), stack);
				}
			});
		}

		//Output slot
		addSlot(new Slot(inv, 9, 133, 33){
			@Override
			public boolean mayPlace(ItemStack stack){
				return false;
			}
		});

		//Recipe slots
		for(int i = 0; i < 9; i++){
			addSlot(new GhostRecipeSlot(inv, 10 + i, 44 + 18 * (i % 3), 15 + 18 * (i / 3)));
		}

		//Hotbar
		for(int i = 0; i < 9; i++){
			addSlot(new Slot(playerInventory, i, 8 + i * 18, 162));
		}

		//Main inventory
		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 9; j++){
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
			}
		}
	}

	@Override
	public boolean stillValid(Player playerIn){
		return inv.stillValid(playerIn);
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int fromSlot){
		ItemStack previous = ItemStack.EMPTY;
		Slot slot = slots.get(fromSlot);

		if(slot != null && slot.hasItem()){
			ItemStack current = slot.getItem();
			previous = current.copy();

			//fromSlot 0-9 means TE -> Player, else Player -> TE input slots
			if(fromSlot < 10 ? !moveItemStackTo(current, 10, 46, true) : !moveItemStackTo(current, 0, 9, false)){
				return ItemStack.EMPTY;
			}

			if(current.isEmpty()){
				slot.set(ItemStack.EMPTY);
			}else{
				slot.setChanged();
			}

			if(current.getCount() == previous.getCount()){
				return ItemStack.EMPTY;
			}
			slot.onTake(playerIn, current);
		}

		return previous;
	}

	@Override
	public void handlePlacement(boolean p_217056_1_, Recipe<?> rec, ServerPlayer player){
		//Called on the server side to set recipe on the clients
		if(te != null){
			te.setRecipe(rec);
		}
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player){
		if(slotId > 9 && slotId < 19 && player != null && te != null){
			if(getCarried().isEmpty()){
				//Click on a recipe slot with an empty cursor

				Recipe<CraftingContainer> rec = te.validateRecipe(AutoCrafterTileEntity.lookupRecipe(te.getRecipeManager(), te.recipe), this);
				if(rec == null){
					inv.removeItemNoUpdate(slotId);
				}else{
					List<Ingredient> ingr = rec.getIngredients();
					int index = slotId - 10;
					int width = rec instanceof IShapedRecipe ? ((IShapedRecipe<CraftingContainer>) rec).getRecipeWidth() : 3;
					if(index % 3 < width && !ingr.get(index - (3 - width) * (index / 3)).isEmpty()){
						//Has an "item" from the recipe in the clicked slot to clear
						if(player.level.isClientSide){
							te.recipe = null;
						}else{
							te.setRecipe(null);
						}
						inv.removeItemNoUpdate(slotId);
					}
				}
			}else{
				//Click on a recipe slot with an item in the cursor

				//Clear any configured recipe
				if(player.level.isClientSide){
					te.recipe = null;
				}else{
					te.setRecipe(null);
				}

				ItemStack s = getCarried().copy();
				s.setCount(1);
				inv.setItem(slotId, s);
			}
		}

		super.clicked(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public List<RecipeBookCategories> getRecipeBookCategories(){
		return AutoCrafterScreen.getRecipeCategories();//Redirect to a method in AutoCrafterScreen as RecipeBookCategories is client only
	}

	@Override
	public RecipeBookType getRecipeBookType(){
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int slot){
		return slot != this.getResultSlotIndex();
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents recipeHelper){
		//Insert a recipe
	}

	@Override
	public void clearCraftingContent(){
		//Empties recipe- no op
	}

	@Override
	public boolean recipeMatches(Recipe<? super CraftingContainer> recipeIn){
		return false;
	}

	@Override
	public int getResultSlotIndex(){
		return 9;
	}

	@Override
	public int getGridWidth(){
		return 3;
	}

	@Override
	public int getGridHeight(){
		return 3;
	}

	@Override
	public int getSize(){
		return 10;
	}

	public static class GhostRecipeSlot extends Slot{

		private GhostRecipeSlot(Container inventoryIn, int index, int xPosition, int yPosition){
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean mayPickup(Player playerIn){
			return false;
		}

		@Override
		public int getMaxStackSize(){
			return 0;
		}

		@Override
		public boolean mayPlace(ItemStack stack){
			return false;
		}
	}
}
