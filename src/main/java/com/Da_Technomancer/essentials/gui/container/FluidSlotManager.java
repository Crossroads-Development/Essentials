package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.platform.GlStateManager;
import jdk.internal.jline.internal.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class FluidSlotManager{

	/**
	 * Keep a map of all registered fluids to a short ID. Of note is that the map is sorted by registry ID to ensure that all clients and the server agree upon mappings without synchronization
	 */
	private static BiMap<ResourceLocation, Short> fluidIDs = null;

	private static BiMap<ResourceLocation, Short> getFluidMap(){
		if(fluidIDs == null){
			fluidIDs = HashBiMap.create();
			ForgeRegistries.FLUIDS.getKeys().stream().sorted(ResourceLocation::compareTo).forEach(key -> fluidIDs.put(key, (short) fluidIDs.size()));
		}
		return fluidIDs;
	}

	//General
	private final int capacity;
	private final IntReferenceHolder fluidIdHolder = IntReferenceHolder.single();
	private final IntReferenceHolder fluidQtyHolder = IntReferenceHolder.single();

	//Per screen
	private int windowXStart;
	private int windowYStart;

	private static final int MAX_HEIGHT = 48;
	private static final ResourceLocation OVERLAY = new ResourceLocation(Essentials.MODID, "textures/gui/rectangle_fluid_overlay.png");

	/**
	 * Keeps a fluidstack synced between the server and open containers on clients. All containers must register trackInt on the two IntReferenceHolders in this class
	 * @param init The initial fluidstack
	 * @param capacity Maximum capacity of this fluidstack
	 */
	public FluidSlotManager(FluidStack init, int capacity){
		this.capacity = capacity;
		fluidIdHolder.set(getFluidMap().getOrDefault(init.getFluid().getRegistryName(), (short) 0));
		fluidQtyHolder.set(init.getAmount() - Short.MAX_VALUE);
	}

	@OnlyIn(Dist.CLIENT)
	public void initScreen(int windowXStart, int windowYStart){
		this.windowXStart = windowXStart;
		this.windowYStart = windowYStart;
	}

	public IntReferenceHolder getFluidIdHolder(){
		return fluidIdHolder;
	}

	public IntReferenceHolder getFluidQtyHolder(){
		return fluidQtyHolder;
	}

	public void updateState(FluidStack newFluid){
		fluidIdHolder.set(getFluidMap().get(newFluid.getFluid().getRegistryName()));
		fluidQtyHolder.set(newFluid.getAmount() - Short.MAX_VALUE);
	}

	@OnlyIn(Dist.CLIENT)
	public FluidStack getStack(){
		Fluid f = ForgeRegistries.FLUIDS.getValue(getFluidMap().inverse().getOrDefault((short) fluidIdHolder.get(), Fluids.WATER.getRegistryName()));
		if(f == null){
			f = Fluids.WATER;
		}
		return new FluidStack(f, fluidQtyHolder.get() + Short.MAX_VALUE);
	}

	@OnlyIn(Dist.CLIENT)
	public void renderBack(int x, int y, float partialTicks, int mouseX, int mouseY, FontRenderer fontRenderer){
		FluidStack clientState = getStack();
		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

		Screen.fill(x + windowXStart, y + windowYStart - MAX_HEIGHT, x + windowXStart + 16, y + windowYStart, 0xFF959595);
		//Screen.fill changes the color
		GlStateManager.color4f(1, 1, 1, 1);

		FluidAttributes attr = clientState.getFluid().getAttributes();

		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureMap().getSprite(attr.getStillTexture());
		//Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		int col = attr.getColor(clientState);
		int height = (int) (MAX_HEIGHT * (float) clientState.getAmount() / (float) capacity);
		GlStateManager.color3f((float) ((col >>> 16) & 0xFF) / 255F, ((float) ((col >>> 8) & 0xFF)) / 255F, ((float) (col & 0xFF)) / 255F);
		Screen.blit(x + windowXStart, y + windowYStart - height, 0, 16, height, sprite);
		GlStateManager.color3f(1, 1, 1);
	}

	@OnlyIn(Dist.CLIENT)
	public void renderFore(int x, int y, int mouseX, int mouseY, FontRenderer fontRenderer, List<String> tooltip){
		FluidStack clientState = getStack();
		Minecraft.getInstance().getTextureManager().bindTexture(OVERLAY);
		Screen.blit(x, y - MAX_HEIGHT, 0, 0, 16, MAX_HEIGHT, 16, MAX_HEIGHT);

		if(mouseX >= x + windowXStart && mouseX <= x + windowXStart + 16 && mouseY >= y + windowYStart - MAX_HEIGHT && mouseY <= y + windowYStart){
			if(clientState.isEmpty()){
				tooltip.add(new TranslationTextComponent("tt.essentials.empty_fluid").getFormattedText());
			}else{
				tooltip.add(clientState.getDisplayName().getFormattedText());
				tooltip.add(clientState.getAmount() + "/" + capacity);
			}
		}
	}

	public static Pair<Slot, Slot> createFluidSlots(IInventory inv, int startIndex, int inXPos, int inYPos, int outXPos, int outYPos, @Nullable IFluidSlotTE te, int[] fluidIndex){
		InSlot in = new InSlot(inv, startIndex + 1, inXPos, inYPos, startIndex, te, fluidIndex);
		OutSlot out = new OutSlot(inv, startIndex, outXPos, outYPos, in);
		return Pair.of(out, in);
	}

	private static class OutSlot extends Slot{

		private final InSlot inSlot;

		private OutSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, InSlot inSlot){
			super(inventoryIn, index, xPosition, yPosition);
			this.inSlot = inSlot;
		}

		@Override
		public boolean isItemValid(ItemStack stack){
			return false;
		}

		@Override
		public int getSlotStackLimit(){
			return 1;
		}

		@Override
		public ItemStack onTake(PlayerEntity p_190901_1_, ItemStack p_190901_2_){
			ItemStack s = super.onTake(p_190901_1_, p_190901_2_);
			inSlot.onSlotChanged();
			return s;
		}
	}

	private static class InSlot extends Slot{

		private final int outSlotIndex;
		@Nullable
		private final IFluidSlotTE te;
		//Array of all indices to interact with
		private final int[] fluidIndices;

		private InSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, int outSlotIndex, @Nullable IFluidSlotTE te, int[] fluidIndices){
			super(inventoryIn, index, xPosition, yPosition);
			this.outSlotIndex = outSlotIndex;
			this.te = te;
			this.fluidIndices = fluidIndices;
		}

		@Override
		public boolean isItemValid(ItemStack stack){
			return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
		}

		@Override
		public int getSlotStackLimit(){
			return 1;
		}

		private boolean internalChange = false;

		@Override
		public void onSlotChanged(){
			super.onSlotChanged();

			ItemStack stack = getStack();
			if(!internalChange && te != null && isItemValid(stack) && inventory.getStackInSlot(outSlotIndex).isEmpty()){
				LazyOptional<IFluidHandlerItem> opt = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
				IFluidHandler handler = te.getFluidHandler();
				if(opt.isPresent()){
					IFluidHandlerItem itHandler = opt.orElseThrow(NullPointerException::new);
					for(int fluidIndex : fluidIndices){
						//Try draining the item
						int maxDrain = handler.getTankCapacity(fluidIndex) - handler.getFluidInTank(fluidIndex).getAmount();
						FluidStack drained = itHandler.drain(maxDrain, IFluidHandler.FluidAction.SIMULATE);
						if(handler.isFluidValid(fluidIndex, drained)){
							maxDrain = handler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
							if(maxDrain != 0){
								itHandler.drain(drained, IFluidHandler.FluidAction.EXECUTE);
								inventory.setInventorySlotContents(outSlotIndex, itHandler.getContainer());
								internalChange = true;
								inventory.setInventorySlotContents(getSlotIndex(), ItemStack.EMPTY);
								internalChange = false;
								inventory.markDirty();
								return;
							}
						}

						//Try filling the item
						//Integer.MAX_VALUE / 2 instead of Integer.MAX_VALUE to prevent possible integer overflow errors in the handler
						FluidStack filled = handler.drain(Integer.MAX_VALUE / 2, IFluidHandler.FluidAction.SIMULATE);
						int filledQty = itHandler.fill(filled, IFluidHandler.FluidAction.EXECUTE);
						if(filledQty != 0){
							filled.setAmount(filledQty);
							handler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
							inventory.setInventorySlotContents(outSlotIndex, itHandler.getContainer());
							internalChange = true;
							inventory.setInventorySlotContents(getSlotIndex(), ItemStack.EMPTY);
							internalChange = false;

							return;
						}
					}
				}
			}
		}
	}

	public static class FakeInventory implements IInventory{

		private final Container container;

		public FakeInventory(Container cont){
			container = cont;
		}

		private final ItemStack[] stacks = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

		@Override
		public int getSizeInventory(){
			return 2;
		}

		@Override
		public boolean isEmpty(){
			return stacks[0].isEmpty() && stacks[1].isEmpty();
		}

		@Override
		public ItemStack getStackInSlot(int index){
			return stacks[index];
		}

		@Override
		public ItemStack decrStackSize(int index, int count){
			markDirty();
			return stacks[index].split(count);
		}

		@Override
		public ItemStack removeStackFromSlot(int index){
			ItemStack stack = stacks[index];
			stacks[index] = ItemStack.EMPTY;
			markDirty();
			return stack;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack){
			stacks[index] = stack;
			markDirty();
		}

		@Override
		public int getInventoryStackLimit(){
			return 64;
		}

		@Override
		public void markDirty(){
			container.detectAndSendChanges();
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity player){
			return true;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack){
			return index == 0 && stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
		}

		@Override
		public void clear(){
			stacks[0] = ItemStack.EMPTY;
			stacks[1] = ItemStack.EMPTY;
			markDirty();
		}
	}
}
