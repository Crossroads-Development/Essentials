package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.AutoCrafterScreen;
import com.Da_Technomancer.essentials.tileentities.AutoCrafterTileEntity;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;

@ObjectHolder(Essentials.MODID)
public class AutoCrafterContainer extends RecipeBookContainer<CraftingInventory>{

	@ObjectHolder("auto_crafter")
	private static ContainerType<AutoCrafterContainer> TYPE = null;

	@Nullable
	public final AutoCrafterTileEntity te;
	private final IInventory inv;
	private final PlayerInventory playerInv;

	public AutoCrafterContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		this(id, playerInventory, new Inventory(19), data.readString(), data.readBlockPos());
	}

	public AutoCrafterContainer(int id, PlayerInventory playerInventory, IInventory inv, String recipeStr, BlockPos pos){
		super(TYPE, id);
		playerInv = playerInventory;
		TileEntity getTe = playerInventory.player.world.getTileEntity(pos);
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
				public boolean isItemValid(ItemStack stack){
					if(te == null){
						return false;
					}
					int freeSlots = te.getLegalSlots(stack.getItem(), inv) - te.getUsedSlots(stack.getItem(), inv);
					return freeSlots > 0 || freeSlots == 0 && BlockUtil.sameItem(getStack(), stack);
				}
			});
		}

		//Output slot
		addSlot(new Slot(inv, 9, 133, 33){
			@Override
			public boolean isItemValid(ItemStack stack){
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
	public boolean canInteractWith(PlayerEntity playerIn){
		return inv.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot){
		ItemStack previous = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(fromSlot);

		if(slot != null && slot.getHasStack()){
			ItemStack current = slot.getStack();
			previous = current.copy();

			//fromSlot 0-9 means TE -> Player, else Player -> TE input slots
			if(fromSlot < 10 ? !mergeItemStack(current, 10, 46, true) : !mergeItemStack(current, 0, 10, false)){
				return ItemStack.EMPTY;
			}

			if(current.isEmpty()){
				slot.putStack(ItemStack.EMPTY);
			}else{
				slot.onSlotChanged();
			}

			if(current.getCount() == previous.getCount()){
				return ItemStack.EMPTY;
			}
			slot.onTake(playerIn, current);
		}

		return previous;
	}

	@Override
	public void func_217056_a(boolean p_217056_1_, IRecipe<?> rec, ServerPlayerEntity player){
		//Called on the server side to set recipe on the clients
		if(te != null){
			te.setRecipe(rec);
		}
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player){
		if(slotId > 9 && slotId < 19 && player != null && te != null){
			if(playerInv.getItemStack().isEmpty()){
				//Click on a recipe slot with an empty cursor

				IRecipe<CraftingInventory> rec = AutoCrafterTileEntity.validateRecipe(te.recipe, te.getRecipeManager());
				if(rec == null){
					inv.removeStackFromSlot(slotId);
				}else{
					List<Ingredient> ingr = rec.getIngredients();
					int index = slotId - 10;
					int width = rec instanceof IShapedRecipe ? ((IShapedRecipe<CraftingInventory>) rec).getRecipeWidth() : 3;
					if(index % 3 < width && !ingr.get(index - (3 - width) * (index / 3)).hasNoMatchingItems()){
						//Has an "item" from the recipe in the clicked slot to clear
						if(player.world.isRemote){
							te.recipe = null;
						}else{
							te.setRecipe(null);
						}
						inv.removeStackFromSlot(slotId);
					}
				}
			}else{
				//Click on a recipe slot with an item in the cursor

				//Clear any configured recipe
				if(player.world.isRemote){
					te.recipe = null;
				}else{
					te.setRecipe(null);
				}

				ItemStack s = playerInv.getItemStack().copy();
				s.setCount(1);
				inv.setInventorySlotContents(slotId, s);
			}
		}else if(slotId >= 0 && slotId < 9 && player != null && te != null){
			//Input slots
			//TODO
		}

		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public List<RecipeBookCategories> getRecipeBookCategories(){
		return AutoCrafterScreen.getRecipeCategories();//Redirect to a method in AutoCrafterScreen as RecipeBookCategories is client only
	}

	@Override
	public void fillStackedContents(RecipeItemHelper recipeHelper){
		//Insert a recipe
	}

	@Override
	public void clear(){
		//Empties recipe- no op
	}

	@Override
	public boolean matches(IRecipe<? super CraftingInventory> recipeIn){
		return false;
	}

	@Override
	public int getOutputSlot(){
		return 9;
	}

	@Override
	public int getWidth(){
		return 3;
	}

	@Override
	public int getHeight(){
		return 3;
	}

	@Override
	public int getSize(){
		return 10;
	}

	public static class GhostRecipeSlot extends Slot{

		private GhostRecipeSlot(IInventory inventoryIn, int index, int xPosition, int yPosition){
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean canTakeStack(PlayerEntity playerIn){
			return false;
		}

		@Override
		public int getSlotStackLimit(){
			return 0;
		}

		@Override
		public boolean isItemValid(ItemStack stack){
			return false;
		}
	}
}
