package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendNBTToClient;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ObjectHolder(Essentials.MODID)
public class AutoCrafterTileEntity extends TileEntity implements INBTReceiver, INamedContainerProvider{

	@ObjectHolder("auto_crafter")
	private static TileEntityType<AutoCrafterTileEntity> TYPE = null;

	/**
	 * Inventory. Slots 0-8 are inputs, Slot 9 is output, slots 10-18 are recipe inputs
	 * Recipe input slots are not accessible by automation, and contain "ghost" items.
	 */
	private final ItemStack[] inv = new ItemStack[19];
	private final Inventory iInv = new Inventory(inv, this);
	private boolean redstone = false;


	@Nullable
	private RecipeManager recipeManager = null;

	@Nullable
	public ResourceLocation recipe;

	public AutoCrafterTileEntity(){
		super(TYPE);
		for(int i = 0; i < 19; i++){
			inv[i] = ItemStack.EMPTY;
		}
	}

	/**
	 * Cached convenience getter for the RecipeManager instance that is safe on both server and client side
	 * @return The RecipeManager instance
	 */
	public RecipeManager getRecipeManager(){
		if(recipeManager == null){
			if(world.isRemote){
				recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
			}else{
				recipeManager = world.getServer().getRecipeManager();
			}
		}

		return recipeManager;
	}

	/**
	 * Checks if the passed recipe is valid, and if so returns it.
	 * @param rec The recipe to validate
	 * @param manager A RecipeManager instance
	 * @return The recipe if it is valid, or null otherwise
	 */
	@Nullable
	public static IRecipe<CraftingInventory> validateRecipe(ResourceLocation rec, RecipeManager manager){
		if(rec == null){
			return null;
		}
		return manager.getRecipe(rec).map(AutoCrafterTileEntity::validateRecipe).orElse(null);
	}

	/**
	 * Gets the currently selected recipe, or null otherwise
	 * @return The current recipe if applicable, or null otherwise
	 */
	@Nullable
	private IRecipe<CraftingInventory> findRecipe(){
		return findRecipe(inv);
	}

	/**
	 * Gets the currently selected recipe, or null otherwise
	 * @param inventory The items to base the recipe off of. Only indices [10, 18] are used
	 * @return The current recipe if applicable, or null otherwise
	 */
	@Nullable
	public IRecipe<CraftingInventory> findRecipe(ItemStack[] inventory){
		IRecipe<CraftingInventory> iRecipe;

		if(recipe == null){
			//No recipe has been directly set via recipe book/JEI. Pick a recipe based on manually configured inputs, if applicable

			//Create a fake inventory with the manually configured inputs for finding a matching recipe
			CraftingInventory fakeInv = new CraftingInventory(new Container(null, 0){
				@Override
				public boolean canInteractWith(PlayerEntity playerIn){
					return false;
				}
			}, 3, 3);
			for(int i = 0; i < 9; i++){
				fakeInv.setInventorySlotContents(i, inventory[i + 10]);
			}
			iRecipe = findRecipe(fakeInv);
		}else{
			//Recipe set via recipe book/JEI
			iRecipe = validateRecipe(recipe, getRecipeManager());
		}
		return iRecipe;
	}

	/**
	 * Gets the currently selected recipe, or null otherwise
	 * @param fakeInv A fake crafting inventory to find a match from
	 * @return The current recipe if applicable, or null otherwise
	 */
	@Nullable
	private IRecipe<CraftingInventory> findRecipe(CraftingInventory fakeInv){
		IRecipe<CraftingInventory> iRecipe;

		if(recipe == null){
			//No recipe has been directly set via recipe book/JEI. Pick a recipe based on manually configured inputs, if applicable
			//Use the recipe manager to find a recipe matching the inputs
			Optional<ICraftingRecipe> recipeOptional = getRecipeManager().getRecipe(IRecipeType.CRAFTING, fakeInv, world);
			iRecipe = validateRecipe(recipeOptional.orElse(null));
		}else{
			//Recipe set via recipe book/JEI
			iRecipe = validateRecipe(recipe, getRecipeManager());
		}
		return iRecipe;
	}

	/**
	 * Checks if the passed recipe is valid, and if so returns it.
	 * @param rec The recipe to validate
	 * @return The recipe if it is valid, or null otherwise
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	private static IRecipe<CraftingInventory> validateRecipe(IRecipe<?> rec){
		if(rec == null || rec.getType() != IRecipeType.CRAFTING || !rec.canFit(3, 3)){
			return null;
		}
		return (IRecipe<CraftingInventory>) rec;
	}

	public void redstoneUpdate(boolean newReds){
		if(newReds != redstone){
			redstone = newReds;
			markDirty();
			MinecraftServer serv;
			if(redstone && world != null && !world.isRemote){
				//Create a fake inventory with the manually configured inputs for finding a matching recipe
				CraftingInventory fakeInv = new CraftingInventory(new Container(null, 0){
					@Override
					public boolean canInteractWith(PlayerEntity playerIn){
						return false;
					}
				}, 3, 3);

				for(int i = 0; i < 9; i++){
					fakeInv.setInventorySlotContents(i, inv[i + 10]);
				}

				//Re-use the newly created fakeInv to save having to re-create it
				IRecipe<CraftingInventory> iRecipe = findRecipe(fakeInv);

				if(iRecipe != null){
					ItemStack output;
					if(recipe != null){
						//If the recipe ID is nonnull, then the fake crafting inv was made with the empty manual input slots, and we should use the generic output
						output = iRecipe.getRecipeOutput();
					}else{
						output = iRecipe.getCraftingResult(fakeInv);
					}

					//Check if the output can fit
					if(inv[9].isEmpty() || BlockUtil.sameItem(inv[9], output) && output.getCount() + inv[9].getCount() <= inv[9].getMaxStackSize()){
						List<Ingredient> ingredients;

						if(recipe == null){
							//Using manual input slots
							ingredients = new ArrayList<>(9);
							for(int i = 10; i < 19; i++){
								if(!inv[i].isEmpty()){
									ingredients.add(Ingredient.fromStacks(inv[i]));
								}
							}
						}else{
							ingredients = iRecipe.getIngredients();
						}

						int[] used = new int[9];

						ingredient:
						for(Ingredient ingr : ingredients){
							if(ingr.hasNoMatchingItems()){
								continue;
							}
							for(int i = 0; i < 9; i++){
								if((inv[i].getCount() - used[i] > 0 && ingr.test(inv[i]))){
									used[i]++;
									continue ingredient;
								}
							}
							//No matching item
							return;
						}

						//Consume ingredients
						for(int i = 0; i < 9; i++){
							inv[i].shrink(used[i]);
						}
						//Produce output
						if(inv[9].isEmpty()){
							inv[9] = output.copy();
						}else{
							inv[9].grow(output.getCount());
						}
					}
				}
			}
		}
	}

	public void dropItems(){
		for(int i = 0; i < 10; i++){
			InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inv[i]);
		}
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);

		String recPath = nbt.getString("recipe");
		if(recPath.isEmpty()){
			recipe = null;
		}else{
			recipe = new ResourceLocation(recPath);
		}

		for(int i = 0; i < 19; ++i){
			if(nbt.contains("slot_" + i)){
				inv[i] = ItemStack.read(nbt.getCompound("slot_" + i));
			}
		}

		redstone = nbt.getBoolean("reds");
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);

		for(int i = 0; i < 19; ++i){
			if(!inv[i].isEmpty()){
				nbt.put("slot_" + i, inv[i].write(new CompoundNBT()));
			}
		}

		if(recipe != null){
			nbt.putString("recipe", recipe.toString());
		}
		nbt.putBoolean("reds", redstone);

		return nbt;
	}

	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt = super.getUpdateTag();
		if(recipe != null){
			nbt.putString("recipe", recipe.toString());
		}
		return nbt;
	}

	private final LazyOptional<IItemHandler> hanOptional = LazyOptional.of(InventoryHandler::new);

	@Override
	public void remove(){
		super.remove();
		hanOptional.invalidate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (LazyOptional<T>) hanOptional;
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
		String str = nbt.getString("recipe");
		if(!str.isEmpty()){
			recipe = new ResourceLocation(str);
		}else{
			recipe = null;
		}

		if(!world.isRemote){
			setRecipe(getRecipeManager().getRecipe(recipe).orElse(null));
		}
		markDirty();
	}

	/**
	 * For server side use only- sets the recipe and updates it on all clients
	 * @param rec The recipe to set
	 */
	public void setRecipe(@Nullable IRecipe<?> rec){
		recipe = rec == null ? null : rec.getId();
		//When setting a recipe, overwrite the manually set recipe input slots
		if(recipe != null){
			for(int i = 10; i < 19; i++){
				inv[i] = ItemStack.EMPTY;
				markDirty();
			}
		}
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("recipe", recipe == null ? "" : recipe.toString());
		BlockUtil.sendClientPacketAround(world, pos, new SendNBTToClient(nbt, pos));
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.auto_crafter");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity player){
		return new AutoCrafterContainer(id, playerInventory, iInv, recipe == null ? "" : recipe.toString(), pos);
	}

	private class InventoryHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 10;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			return slot < 10 ? inv[slot] : ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot >= 9 || stack.isEmpty()){
				return stack;
			}

			//Only allow inserting items into a slot if there is no other slot with the same item or that item type is already in the slot
			if(!BlockUtil.sameItem(stack, inv[slot])){
				for(int i = 0; i < 9; i++){
					if(BlockUtil.sameItem(inv[i], stack)){
						return stack;
					}
				}
			}

			int change = Math.min(stack.getMaxStackSize() - inv[slot].getCount(), stack.getCount());

			if(!simulate){
				if(inv[slot].isEmpty()){
					inv[slot] = stack.copy();
				}else{
					inv[slot].grow(change);
				}
			}

			ItemStack out = stack.copy();
			out.shrink(change);
			return stack.getCount() == change ? ItemStack.EMPTY : out;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(slot == 9 && !inv[slot].isEmpty()){
				int change = Math.min(inv[9].getCount(), amount);
				ItemStack out = inv[9].copy();
				out.setCount(change);

				if(!simulate){
					inv[9].shrink(change);
				}

				return change == 0 ? ItemStack.EMPTY : out;
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return slot < 10 ? 64 : 0;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return slot < 9;
		}
	}


	public static class Inventory implements IInventory{

		private final ItemStack[] inv;
		@Nullable
		private final AutoCrafterTileEntity te;

		private Inventory(ItemStack[] inv, @Nullable AutoCrafterTileEntity te){
			this.inv = inv;
			this.te = te;
		}

		@Override
		public int getSizeInventory(){
			return inv.length;
		}

		@Override
		public ItemStack getStackInSlot(int index){
			return index >= inv.length ? ItemStack.EMPTY : inv[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count){
			if(index >= inv.length || inv[index].isEmpty()){
				return ItemStack.EMPTY;
			}

			return inv[index].split(count);
		}

		@Override
		public ItemStack removeStackFromSlot(int index){
			if(index >= inv.length){
				return ItemStack.EMPTY;
			}

			ItemStack stack = inv[index].copy();
			inv[index].setCount(0);
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack){
			if(index < inv.length){
				inv[index] = stack;
			}
		}

		@Override
		public int getInventoryStackLimit(){
			return 64;
		}

		@Override
		public void markDirty(){
			if(te != null){
				te.markDirty();
			}
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity playerEntity){
			return true;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack){
			return index < inv.length - 1;
		}

		@Override
		public void clear(){
			for(int i = 0; i < inv.length; i++){
				inv[i] = ItemStack.EMPTY;
			}
		}

		@Override
		public boolean isEmpty(){
			for(ItemStack itemStack : inv){
				if(!itemStack.isEmpty()){
					return false;
				}
			}
			return true;
		}
	}
}
