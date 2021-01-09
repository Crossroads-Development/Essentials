package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class FluidSlotManager{

	/**
	 * Keep a map of all registered fluids to a short ID. Of note is that the map is sorted by registry ID to ensure that all clients and the server agree upon mappings without synchronization
	 * No entry with value -1 is stored, and an id of -1 should be treated as meaning 'empty fluidstack'
	 * This map does contain "minecraft:empty"
	 */
	private static BiMap<ResourceLocation, Short> fluidIDs = null;

	private static BiMap<ResourceLocation, Short> getFluidMap(){
		if(fluidIDs == null){
			fluidIDs = HashBiMap.create(ForgeRegistries.FLUIDS.getKeys().size());
			//As execution order is important, this cannot work as a parallel stream
			//This must have the exact same result on the server and client sides
			final short[] value = {0};
			ForgeRegistries.FLUIDS.getKeys().stream().sorted(ResourceLocation::compareTo).forEach(key -> fluidIDs.put(key, value[0]++));
		}
		return fluidIDs;
	}

	//General
	private final int capacity;
	private int fluidId;
	private int fluidQty;//Offset by Short.MAX_VALUE to pack more info in
	/**
	 * A list of all fluid-item input slots associated with this fluid slot manager
	 * As multiple container instances can be active at once on the server-side due to multiple players having the UI open, this needs to be a list.
	 * WeakReferences are used to not force old containers to remain in memory, as there is no way to check for expiration of slots
	 *
	 * Only read from on the virtual-server side
	 */
	private final ArrayList<WeakReference<Slot>> fluidItemInSlots = new ArrayList<>(1);


	//Per screen
	private int windowXStart;
	private int windowYStart;
	private int xPos;
	private int yPos;
	private IntReferenceHolder idRef;
	private IntReferenceHolder qtyRef;


	private static final int MAX_HEIGHT = 48;
	private static final ResourceLocation OVERLAY = new ResourceLocation(Essentials.MODID, "textures/gui/rectangle_fluid_overlay.png");

	/**
	 * Keeps a fluidstack synced between the server and open containers on clients. All containers must register trackInt on the two IntReferenceHolders in this class
	 * @param init The initial fluidstack
	 * @param capacity Maximum capacity of this fluidstack
	 */
	public FluidSlotManager(FluidStack init, int capacity){
		this.capacity = capacity;
		fluidId = getFluidMap().getOrDefault(init.getFluid().getRegistryName(), (short) -1);
		fluidQty = init.getAmount() - Short.MAX_VALUE;
	}

	@OnlyIn(Dist.CLIENT)
	public void initScreen(int windowXStart, int windowYStart, int xPos, int yPos, IntReferenceHolder idRef, IntReferenceHolder qtyRef){
		this.windowXStart = windowXStart;
		this.windowYStart = windowYStart;
		this.xPos = xPos;
		this.yPos = yPos;
		this.idRef = idRef;
		this.qtyRef = qtyRef;
		//Sets them to previously initialized values (empty) to prevent getting 0,0 values from before they are updated by packets
		this.idRef.set(fluidId);
		this.qtyRef.set(fluidQty);
	}

	public void linkSlot(Slot fluidItemInputSlot){
		fluidItemInSlots.add(new WeakReference<>(fluidItemInputSlot));
	}

	public int getFluidId(){
		return fluidId;
	}

	public int getFluidQty(){
		return fluidQty;
	}

	public void updateState(FluidStack newFluid){
		fluidId = getFluidMap().getOrDefault(newFluid.getFluid().getRegistryName(), (short) -1);
		fluidQty = newFluid.getAmount() - Short.MAX_VALUE;

		for(int index = 0; index < fluidItemInSlots.size(); index++){
			Slot contents = fluidItemInSlots.get(index).get();
			if(contents == null){
				fluidItemInSlots.remove(index);
				index--;
			}else{
				contents.onSlotChanged();
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public FluidStack getStack(){
		short fluidId = (short) idRef.get();
		if(fluidId < 0){
			return FluidStack.EMPTY;
		}
		//These values default to water to prevent the possibility of crashing if any registry is corrupted
		Fluid f = ForgeRegistries.FLUIDS.getValue(getFluidMap().inverse().getOrDefault(fluidId, Fluids.WATER.getRegistryName()));
		if(f == null){
			f = Fluids.WATER;
		}
		return new FluidStack(f, qtyRef.get() + Short.MAX_VALUE);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(MatrixStack matrix, float partialTicks, int mouseX, int mouseY, FontRenderer fontRenderer, List<ITextComponent> tooltip){
		//Background
		FluidStack clientState = getStack();
		Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);

		Screen.fill(matrix, xPos + windowXStart, yPos + windowYStart - MAX_HEIGHT, xPos + windowXStart + 16, yPos + windowYStart, 0xFF959595);
		//Screen.fill changes the color
		RenderSystem.color4f(1, 1, 1, 1);

		FluidAttributes attr = clientState.getFluid().getAttributes();

		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(attr.getStillTexture());
		int col = attr.getColor(clientState);
		int height = (int) (MAX_HEIGHT * (float) clientState.getAmount() / (float) capacity);
		RenderSystem.color3f((float) ((col >>> 16) & 0xFF) / 255F, ((float) ((col >>> 8) & 0xFF)) / 255F, ((float) (col & 0xFF)) / 255F);
		Screen.blit(matrix, xPos + windowXStart, yPos + windowYStart - height, 0, 16, height, sprite);
		RenderSystem.color3f(1, 1, 1);

		//Foreground
		Minecraft.getInstance().getTextureManager().bindTexture(OVERLAY);
		Screen.blit(matrix, windowXStart + xPos, windowYStart + yPos - MAX_HEIGHT, 0, 0, 16, MAX_HEIGHT, 16, MAX_HEIGHT);

		if(mouseX >= xPos + windowXStart && mouseX <= xPos + windowXStart + 16 && mouseY >= yPos + windowYStart - MAX_HEIGHT && mouseY <= yPos + windowYStart){
			if(clientState.isEmpty()){
				tooltip.add(new TranslationTextComponent("tt.essentials.fluid_contents.empty"));
			}else{
				tooltip.add(clientState.getDisplayName());
			}
			tooltip.add(new TranslationTextComponent("tt.essentials.fluid_contents", clientState.getAmount(), capacity));
		}
	}

	/**
	 * Creates a pair of (self-managing) slots for fluid containers to interact with fluid handlers
	 * @param inv A fake inventory instance
	 * @param startIndex The index to assign to the output slot (input slot will use startIndex + 1)
	 * @param inXPos X st position of the input slot (UI relative)
	 * @param inYPos Y st position of the input slot (UI relative)
	 * @param outXPos X st position of the output slot (UI relative)
	 * @param outYPos Y st position of the output slot (UI relative)
	 * @param te The TE with fluid handler these slots link to
	 * @param fluidIndex The indices of the fluid handler returned by the TE to interact with
	 * @return A pair containing the output slot followed by the input slot, to be added to the container in that order
	 */
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
		public ItemStack onTake(PlayerEntity player, ItemStack stack){
			ItemStack s = super.onTake(player, stack);
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
		private boolean internalChange = false;//Used to prevent infinite recursive loops with onSlotChanged


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
		public void onSlotChanged(){
			super.onSlotChanged();

			ItemStack inSlot = getStack();

			if(!internalChange && te != null && isItemValid(inSlot)){
				internalChange = true;
				ItemStack outSlot = inventory.getStackInSlot(outSlotIndex);
				ItemStack inSlotCopy = inSlot.copy();//We make a copy of the inSlot so we can restore in case this fails
				inSlotCopy.setCount(1);//Size needs to be one or item fluid capabilities refuse to work
				LazyOptional<IFluidHandlerItem> opt = inSlotCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
				IFluidHandler teHandler = te.getFluidHandler();
				if(opt.isPresent()){
					IFluidHandlerItem itemHandler = opt.orElseThrow(NullPointerException::new);

					//'Simple' route- we don't have to verify the output item
					if(outSlot.isEmpty()){
						//We try each fluid index, and stop when we have 1 effective transfer
						for(int fluidIndex : fluidIndices){
							//'Trust but verify' doesn't really apply- with IFluidHandlerItem, we can't even trust; Thus the following convolution
							//We assume the teHandler will be well behaved
							//We assume the itemHandler will have weird restrictions like minimum fluid increments

							//Try draining the item
							int drainQty = teHandler.getTankCapacity(fluidIndex) - teHandler.getFluidInTank(fluidIndex).getAmount();
							FluidStack drained = itemHandler.drain(drainQty, IFluidHandler.FluidAction.SIMULATE);
							if(teHandler.isFluidValid(fluidIndex, drained)){
								drainQty = teHandler.fill(drained, IFluidHandler.FluidAction.SIMULATE);
								//Make sure the item will actually allow draining this quantity of fluid, and perform the withdrawal
								drained = itemHandler.drain(drainQty, IFluidHandler.FluidAction.EXECUTE);
								drainQty = drained.getAmount();
								if(drainQty > 0){
									teHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
									inventory.setInventorySlotContents(outSlotIndex, itemHandler.getContainer());
									inSlot.shrink(1);
									inventory.setInventorySlotContents(getSlotIndex(), inSlot);
									inventory.markDirty();

									internalChange = false;
									return;
								}
							}

							//Try filling the item
							//Integer.MAX_VALUE / 2 instead of Integer.MAX_VALUE to prevent possible integer overflow errors in the handler
							//Find how much the te can provide
							FluidStack filled = teHandler.drain(Integer.MAX_VALUE / 2, IFluidHandler.FluidAction.SIMULATE);
							//Fill as much as allowed, but only drain as much from the te as was actually filled
							int filledQty = itemHandler.fill(filled, IFluidHandler.FluidAction.EXECUTE);
							if(filledQty > 0){
								filled.setAmount(filledQty);
								teHandler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
								inventory.setInventorySlotContents(outSlotIndex, itemHandler.getContainer());
								inSlot.shrink(1);
								inventory.setInventorySlotContents(getSlotIndex(), inSlot);

								internalChange = false;
								return;
							}
						}
					}else{
						//We attempt to move fluid with the item, check if the resulting container item will stack in the output, and if not, reverse actions
						//We try each fluid index, and stop when we have 1 effective transfer
						for(int fluidIndex : fluidIndices){
							//We assume the teHandler will be well behaved
							//We assume the itemHandler will have weird restrictions like minimum fluid increments

							//Try draining the item
							int drainQty = teHandler.getTankCapacity(fluidIndex) - teHandler.getFluidInTank(fluidIndex).getAmount();
							FluidStack drained = itemHandler.drain(drainQty, IFluidHandler.FluidAction.SIMULATE);
							if(teHandler.isFluidValid(fluidIndex, drained)){
								drainQty = teHandler.fill(drained, IFluidHandler.FluidAction.SIMULATE);
								//Make sure the item will actually allow draining this quantity of fluid, and perform the withdrawal
								drained = itemHandler.drain(drainQty, IFluidHandler.FluidAction.EXECUTE);
								drainQty = drained.getAmount();
								ItemStack containerResult = itemHandler.getContainer();
								if(drainQty > 0 && (containerResult.isEmpty() || BlockUtil.sameItem(containerResult, outSlot) && outSlot.getCount() + containerResult.getCount() <= containerResult.getMaxStackSize())){
									teHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
									outSlot.grow(containerResult.getCount());
									inventory.setInventorySlotContents(outSlotIndex, outSlot);
									inSlot.shrink(1);
									inventory.setInventorySlotContents(getSlotIndex(), inSlot);
									inventory.markDirty();

									internalChange = false;
									return;
								}else{
									//Failed- revert the changes and continue
									inSlotCopy = inSlot.copy();
									inSlotCopy.setCount(1);
									opt = inSlotCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
									itemHandler = opt.orElseThrow(NullPointerException::new);
									inventory.setInventorySlotContents(getSlotIndex(), inSlot);
									//no markDirty, as the final result is the same as the start state
								}
							}

							//Try filling the item
							//Integer.MAX_VALUE / 2 instead of Integer.MAX_VALUE to prevent possible integer overflow errors in the handler
							//Find how much the te can provide
							FluidStack filled = teHandler.drain(Integer.MAX_VALUE / 2, IFluidHandler.FluidAction.SIMULATE);
							//Fill as much as allowed, but only drain as much from the te as was actually filled
							int filledQty = itemHandler.fill(filled, IFluidHandler.FluidAction.EXECUTE);
							ItemStack containerResult = itemHandler.getContainer();
							if(filledQty > 0 && (containerResult.isEmpty() || BlockUtil.sameItem(containerResult, outSlot) && outSlot.getCount() + containerResult.getCount() <= containerResult.getMaxStackSize())){
								filled.setAmount(filledQty);
								teHandler.drain(filled, IFluidHandler.FluidAction.EXECUTE);
								outSlot.grow(containerResult.getCount());
								inventory.setInventorySlotContents(outSlotIndex, outSlot);
								inSlot.shrink(1);
								inventory.setInventorySlotContents(getSlotIndex(), inSlot);

								internalChange = false;
								return;
							}else{
								//Failed- revert the changes and continue
								inSlotCopy = inSlot.copy();
								inSlotCopy.setCount(1);
								opt = inSlotCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
								itemHandler = opt.orElseThrow(NullPointerException::new);
								inventory.setInventorySlotContents(getSlotIndex(), inSlot);
								//no markDirty, as the final result is the same as the start state
							}
						}
					}

				}
				internalChange = false;
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
