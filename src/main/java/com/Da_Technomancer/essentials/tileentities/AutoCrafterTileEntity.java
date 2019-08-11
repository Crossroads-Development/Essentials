package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
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
import java.util.Optional;

@ObjectHolder(Essentials.MODID)
public class AutoCrafterTileEntity extends TileEntity implements INBTReceiver, INamedContainerProvider{

	@ObjectHolder("auto_crafter")
	private static TileEntityType<AutoCrafterTileEntity> TYPE = null;

	/**
	 * Inventory. Slots 0-8 are inputs, Slot 9 is output
	 */
	private final ItemStack[] inv = new ItemStack[10];
	private boolean redstone = false;

	@Nullable
	public ResourceLocation recipe;

	public AutoCrafterTileEntity(){
		super(TYPE);
		for(int i = 0; i < 10; i++){
			inv[i] = ItemStack.EMPTY;
		}
	}

	public void redstoneUpdate(boolean newReds){
		if(newReds != redstone){
			redstone = newReds;
			markDirty();
			if(redstone && !world.isRemote && recipe != null){
				Optional<? extends IRecipe> recipeResult = world.getServer().getRecipeManager().getRecipe(recipe);

				//Confirm the recipe exists
				if(recipeResult.isPresent()){
					IRecipe<?> iRecipe = recipeResult.get();
					//Make sure the configured recipe is for the standard crafting table
					if(iRecipe.getType() == IRecipeType.CRAFTING && iRecipe.canFit(3, 3)){
						//Check the output can fit
						ItemStack output = iRecipe.getRecipeOutput();
						if(inv[9].isEmpty() || BlockUtil.sameItem(inv[9], output) && output.getCount() + inv[9].getCount() <= inv[9].getMaxStackSize()){
							NonNullList<Ingredient> ingredients = iRecipe.getIngredients();
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
	}

	@Override
	public void read(CompoundNBT nbt){
		super.read(nbt);

		for(int i = 0; i < 10; ++i){
			if(nbt.contains("slot_" + i)){
				inv[i] = ItemStack.read(nbt.getCompound("slot_" + i));
			}
		}
		String recPath = nbt.getString("recipe");
		if(!recPath.isEmpty()){
			recipe = new ResourceLocation(recPath);
		}
		redstone = nbt.getBoolean("reds");
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);

		for(int i = 0; i < 10; ++i){
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

	private final Inventory iInv = new Inventory(inv, this);

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return LazyOptional.of(() -> (T) new InventoryHandler());
		}

		return super.getCapability(cap, facing);
	}

	@Override
	public void receiveNBT(CompoundNBT nbt){
		String str = nbt.getString("recipe");
		if(!str.isEmpty()){
			recipe = new ResourceLocation(str);
		}else{
			recipe = null;
		}
		markDirty();
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

		public Inventory(ItemStack[] inv, @Nullable AutoCrafterTileEntity te){
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
