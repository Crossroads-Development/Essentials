package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import com.Da_Technomancer.essentials.packets.SendNBTToClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ObjectHolder(Essentials.MODID)
public class AutoCrafterTileEntity extends BlockEntity implements INBTReceiver, MenuProvider{

	@ObjectHolder("auto_crafter")
	public static BlockEntityType<AutoCrafterTileEntity> TYPE = null;

	/**
	 * Inventory. Slots 0-8 are inputs, Slot 9 is output, slots 10-18 are recipe inputs
	 * Recipe input slots are not accessible by automation, and contain "ghost" items.
	 */
	protected final ItemStack[] inv = new ItemStack[invSize()];
	protected final CrafterInventory iInv = new CrafterInventory(inv, this);
	protected boolean redstone = false;


	@Nullable
	protected RecipeManager recipeManager = null;

	@Nullable
	public ResourceLocation recipe;

	public AutoCrafterTileEntity(BlockPos pos, BlockState state){
		this(TYPE, pos, state);
	}

	protected AutoCrafterTileEntity(BlockEntityType<? extends AutoCrafterTileEntity> type, BlockPos pos, BlockState state){
		super(type, pos, state);
		Arrays.fill(inv, ItemStack.EMPTY);
	}

	protected int invSize(){
		return 19;
	}

	/**
	 * Cached convenience getter for the RecipeManager instance that is safe on both server and client side
	 * @return The RecipeManager instance
	 */
	public RecipeManager getRecipeManager(){
		if(recipeManager == null){
			if(level.isClientSide){
				recipeManager = Minecraft.getInstance().getConnection().getRecipeManager();
			}else{
				recipeManager = level.getServer().getRecipeManager();
			}
		}

		return recipeManager;
	}

	@Nullable
	public static Recipe<?> lookupRecipe(RecipeManager manager, ResourceLocation recipe){
		return recipe == null ? null : manager.byKey(recipe).orElse(null);
	}

	/**
	 * Makes a crafting inventory configured with the passed items
	 * @param inv A size 19 or more array, uses indices [10, 19]
	 * @return A crafting inventory
	 */
	public static CraftingContainer prepareCraftingInv(ItemStack[] inv){
		CraftingContainer craftInv = new CraftingContainer(new AbstractContainerMenu(null, 0){
			@Override
			public boolean stillValid(Player playerIn){
				return false;
			}
		}, 3, 3);
		for(int i = 0; i < 9; i++){
			craftInv.setItem(i, inv[i + 10]);
		}
		return craftInv;
	}

	/**
	 * Gets the currently selected recipe, or null otherwise
	 * @param fakeInv A fake crafting inventory to find a match from
	 * @param container The calling container, if called via UI
	 * @return The current recipe if applicable, or null otherwise
	 */
	@Nullable
	public Recipe<CraftingContainer> findRecipe(CraftingContainer fakeInv, @Nullable AutoCrafterContainer container){
		Recipe<CraftingContainer> iRecipe;

		if(recipe == null){
			//No recipe has been directly set via recipe book/JEI. Pick a recipe based on manually configured inputs, if applicable
			//Use the recipe manager to find a recipe matching the inputs
			Optional<CraftingRecipe> recipeOptional = getRecipeManager().getRecipeFor(RecipeType.CRAFTING, fakeInv, level);
			iRecipe = validateRecipe(recipeOptional.orElse(null), container);
		}else{
			//Recipe set via recipe book/JEI
			iRecipe = validateRecipe(lookupRecipe(getRecipeManager(), recipe), container);
		}
		return iRecipe;
	}

	/**
	 * Checks if the passed recipe is valid, and if so returns it.
	 * @param rec The recipe to validate
	 * @param container The calling container, if called via UI
	 * @return The recipe if it is valid, or null otherwise
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public Recipe<CraftingContainer> validateRecipe(Recipe<?> rec, @Nullable AutoCrafterContainer container){
		if(rec == null || rec.getType() != RecipeType.CRAFTING || !rec.canCraftInDimensions(3, 3)){
			return null;
		}
		return (Recipe<CraftingContainer>) rec;
	}

	public void redstoneUpdate(boolean newReds){
		if(newReds != redstone){
			redstone = newReds;
			setChanged();
			if(redstone && level != null && !level.isClientSide){
				//Create a fake inventory with the manually configured inputs for finding a matching recipe
				CraftingContainer fakeInv = prepareCraftingInv(inv);

				//Re-use the newly created fakeInv to save having to re-create it
				Recipe<CraftingContainer> iRecipe = findRecipe(fakeInv, null);

				if(iRecipe != null){
					ItemStack output;
					if(recipe != null){
						//If the recipe ID is nonnull, then the fake crafting inv was made with the empty manual input slots, and we should use the generic output
						output = iRecipe.getResultItem();
					}else{
						output = iRecipe.assemble(fakeInv);
					}

					//Check if the output can fit
					if(inv[9].isEmpty() || BlockUtil.sameItem(inv[9], output) && output.getCount() + inv[9].getCount() <= inv[9].getMaxStackSize()){
						List<Ingredient> ingredients;

						if(recipe == null){
							//Using manual input slots
							ingredients = new ArrayList<>(9);
							for(int i = 10; i < 19; i++){
								if(!inv[i].isEmpty()){
									ingredients.add(Ingredient.of(inv[i]));
								}
							}
						}else{
							ingredients = iRecipe.getIngredients();
						}

						int[] used = new int[9];

						ingredient:
						for(Ingredient ingr : ingredients){
							if(ingr.isEmpty()){
								continue;
							}
							//Count index down instead of up to use the last slots first
							for(int i = 8; i >= 0; i--){
								if((inv[i].getCount() - used[i] > 0 && ingr.test(inv[i]))){
									used[i]++;
									continue ingredient;
								}
							}
							//No matching item- abort the craft
							return;
						}

						List<ItemStack> containers = new ArrayList<>(0);
						//Consume ingredients
						for(int i = 0; i < 9; i++){
							if(used[i] != 0 && inv[i].hasContainerItem()){
								ItemStack cont = inv[i].getContainerItem().copy();
								cont.setCount(used[i]);
								containers.add(cont);
							}
							inv[i].shrink(used[i]);
						}
						//Produce output
						if(inv[9].isEmpty()){
							inv[9] = output.copy();
						}else{
							inv[9].grow(output.getCount());
						}
						for(ItemStack s : containers){
							int slot = -1;
							for(int i = 0; i < 9; i++){
								if(BlockUtil.sameItem(s, inv[i])){
									slot = i;
									break;
								}
								if(inv[i].isEmpty()){
									slot = i;
									break;
								}
							}
							if(slot == -1){
								//Item can't fit in the input slots- eject
								Containers.dropItemStack(level, worldPosition.getX() + Math.random(), worldPosition.getY() + Math.random(), worldPosition.getZ() + Math.random(), s);
							}else if(inv[slot].isEmpty()){
								inv[slot] = s;
							}else{
								inv[slot].grow(s.getCount());
							}
						}
					}
				}
			}
		}
	}

	public void dropItems(){
		for(int i = 0; i < 10; i++){
			Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inv[i]);
		}
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);

		String recPath = nbt.getString("recipe");
		if(recPath.isEmpty()){
			recipe = null;
		}else{
			recipe = new ResourceLocation(recPath);
		}

		for(int i = 0; i < inv.length; ++i){
			if(nbt.contains("slot_" + i)){
				inv[i] = ItemStack.of(nbt.getCompound("slot_" + i));
			}
		}

		redstone = nbt.getBoolean("reds");
	}

	@Override
	public CompoundTag save(CompoundTag nbt){
		super.save(nbt);

		for(int i = 0; i < inv.length; ++i){
			if(!inv[i].isEmpty()){
				nbt.put("slot_" + i, inv[i].save(new CompoundTag()));
			}
		}

		if(recipe != null){
			nbt.putString("recipe", recipe.toString());
		}
		nbt.putBoolean("reds", redstone);

		return nbt;
	}

	@Override
	public CompoundTag getUpdateTag(){
		CompoundTag nbt = super.getUpdateTag();
		if(recipe != null){
			nbt.putString("recipe", recipe.toString());
		}
		return nbt;
	}

	public int getLegalSlots(Item item, Container inv, @Nullable AutoCrafterContainer container){
		//The maximum number of slots an item type can use is equal to the number of items in the recipe input
		int count = 0;
		if(recipe == null){
			//If recipe was set via ghost items, use the manual config
			for(int i = 10; i < 19; i++){//10-18 are the ghost recipe input
				if(inv.getItem(i).getItem() == item){
					count++;
				}
			}
		}else{
			Recipe<CraftingContainer> rec = validateRecipe(lookupRecipe(getRecipeManager(), recipe), container);
			if(rec != null){
				ItemStack testStack = new ItemStack(item, 1);
				for(Ingredient ingr : rec.getIngredients()){
					if(ingr.test(testStack)){
						count++;
					}
				}
			}
		}
		return count;
	}

	public int getUsedSlots(Item item, Container inv){
		int count = 0;
		for(int i = 0; i < 9; i++){
			if(inv.getItem(i).getItem() == item){
				count++;
			}
		}
		return count;
	}

	private final LazyOptional<IItemHandler> hanOptional = LazyOptional.of(InventoryHandler::new);

	@Override
	public void setRemoved(){
		super.setRemoved();
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
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		String str = nbt.getString("recipe");
		if(!str.isEmpty()){
			recipe = new ResourceLocation(str);
		}else{
			recipe = null;
		}

		if(!level.isClientSide){
			setRecipe(lookupRecipe(getRecipeManager(), recipe));
		}
		setChanged();
	}

	/**
	 * For server side use only- sets the recipe and updates it on all clients
	 * @param rec The recipe to set
	 */
	public void setRecipe(@Nullable Recipe<?> rec){
		recipe = rec == null ? null : rec.getId();
		//When setting a recipe, overwrite the manually set recipe input slots
		if(recipe != null){
			for(int i = 10; i < 19; i++){
				inv[i] = ItemStack.EMPTY;
				setChanged();
			}
		}
		CompoundTag nbt = new CompoundTag();
		nbt.putString("recipe", recipe == null ? "" : recipe.toString());
		BlockUtil.sendClientPacketAround(level, worldPosition, new SendNBTToClient(nbt, worldPosition));
	}

	@Override
	public Component getDisplayName(){
		return new TranslatableComponent("container.auto_crafter");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player){
		return new AutoCrafterContainer(id, playerInventory, iInv, worldPosition);
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
			Item item = stack.getItem();
			if(inv[slot].isEmpty() && getLegalSlots(item, iInv, null) <= getUsedSlots(item, iInv)){
				return stack;
			}
			if(!BlockUtil.sameItem(stack, inv[slot]) && !inv[slot].isEmpty()){
				return stack;
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
			//Allow extraction from the output AND input slots with items that aren't in the recipe
			if(slot == 9 && !inv[slot].isEmpty()){
				int change = Math.min(inv[9].getCount(), amount);
				ItemStack out = inv[9].copy();
				out.setCount(change);

				if(!simulate){
					inv[9].shrink(change);
				}

				return change == 0 ? ItemStack.EMPTY : out;
			}

			//Only allow removing from an input slot if it contains an item above the limit for that type
			if(slot < 9 && slot >= 0 && !inv[slot].isEmpty()){
				Item item = inv[slot].getItem();
				if(getUsedSlots(item, iInv) > getLegalSlots(item, iInv, null)){
					int change = Math.min(inv[slot].getCount(), amount);
					ItemStack out = inv[slot].copy();
					out.setCount(change);

					if(!simulate){
						inv[slot].shrink(change);
					}

					return change == 0 ? ItemStack.EMPTY : out;
				}
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


	public static class CrafterInventory implements Container{

		private final ItemStack[] inv;
		@Nullable
		private final AutoCrafterTileEntity te;

		private CrafterInventory(ItemStack[] inv, @Nullable AutoCrafterTileEntity te){
			this.inv = inv;
			this.te = te;
		}

		@Override
		public int getContainerSize(){
			return inv.length;
		}

		@Override
		public ItemStack getItem(int index){
			return index >= inv.length ? ItemStack.EMPTY : inv[index];
		}

		@Override
		public ItemStack removeItem(int index, int count){
			if(index >= inv.length || inv[index].isEmpty()){
				return ItemStack.EMPTY;
			}

			return inv[index].split(count);
		}

		@Override
		public ItemStack removeItemNoUpdate(int index){
			if(index >= inv.length){
				return ItemStack.EMPTY;
			}

			ItemStack stack = inv[index].copy();
			inv[index].setCount(0);
			return stack;
		}

		@Override
		public void setItem(int index, ItemStack stack){
			if(index < inv.length){
				inv[index] = stack;
			}
		}

		@Override
		public int getMaxStackSize(){
			return 64;
		}

		@Override
		public void setChanged(){
			if(te != null){
				te.setChanged();
			}
		}

		@Override
		public boolean stillValid(Player playerEntity){
			return true;
		}

		@Override
		public boolean canPlaceItem(int index, ItemStack stack){
			return index < inv.length - 1;
		}

		@Override
		public void clearContent(){
			Arrays.fill(inv, ItemStack.EMPTY);
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
