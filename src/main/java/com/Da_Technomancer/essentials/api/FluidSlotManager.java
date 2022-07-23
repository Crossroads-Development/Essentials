package com.Da_Technomancer.essentials.api;

import com.Da_Technomancer.essentials.Essentials;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
			final AtomicReference<Short> value = new AtomicReference<>((short) 0);
			ForgeRegistries.FLUIDS.getKeys().stream().sorted(ResourceLocation::compareTo).forEach(key -> {
				short newId = value.get();
				value.set((short) (newId + 1));
				try{
					fluidIDs.put(key, newId);
				}catch(IllegalArgumentException e){
					Essentials.logger.log(Level.ERROR, "Duplicate while creating fluid map; report to mod author");
					Essentials.logger.log(Level.ERROR, "Entry being added: " + key.toString() + " -> " + newId);
					Essentials.logger.log(Level.ERROR, "Fluid bi-map dump:");
					Essentials.logger.log(Level.ERROR, fluidIDs);
					Essentials.logger.log(Level.ERROR, "Stacktrace:", e);
				}
			});
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
	private DataSlot idRef;
	private DataSlot qtyRef;


	private static final int MAX_HEIGHT = 48;
	private static final ResourceLocation OVERLAY = new ResourceLocation(Essentials.MODID, "textures/gui/rectangle_fluid_overlay.png");

	/**
	 * Keeps a fluidstack synced between the server and open containers on clients. All containers must register trackInt on the two IntReferenceHolders in this class
	 * @param init The initial fluidstack
	 * @param capacity Maximum capacity of this fluidstack
	 */
	public FluidSlotManager(FluidStack init, int capacity){
		this.capacity = capacity;
		fluidId = getFluidMap().getOrDefault(ForgeRegistries.FLUIDS.getKey(init.getFluid()), (short) -1);
		fluidQty = init.getAmount() - Short.MAX_VALUE;
	}


	/**
	 * Only call on the virtual client side
	 * @param windowXStart Window x start position
	 * @param windowYStart Window y start position
	 * @param xPos Left-most X position of the fluid bar
	 * @param yPos Bottom-most Y position of the fluid bar
	 * @param idRef Reference tracking fluid type
	 * @param qtyRef Reference tracking fluid quantity
	 */
	public void initScreen(int windowXStart, int windowYStart, int xPos, int yPos, DataSlot idRef, DataSlot qtyRef){
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
		fluidId = getFluidMap().getOrDefault(ForgeRegistries.FLUIDS.getKey(newFluid.getFluid()), (short) -1);
		fluidQty = newFluid.getAmount() - Short.MAX_VALUE;

		for(int index = 0; index < fluidItemInSlots.size(); index++){
			Slot contents = fluidItemInSlots.get(index).get();
			if(contents == null){
				fluidItemInSlots.remove(index);
				index--;
			}else{
				contents.setChanged();
			}
		}
	}

	/**
	 * Gets the fluidstack represented by this fluid inventory
	 *
	 * Call on the virtual client side only
	 */
	public FluidStack getStack(){
		short fluidId = (short) idRef.get();
		if(fluidId < 0){
			return FluidStack.EMPTY;
		}
		//These values default to empty to prevent the possibility of crashing if any registry is corrupted
		Fluid f = ForgeRegistries.FLUIDS.getValue(getFluidMap().inverse().get(fluidId));
		if(f == null){
			f = Fluids.EMPTY;
		}
		return new FluidStack(f, qtyRef.get() + Short.MAX_VALUE);
	}

	@OnlyIn(Dist.CLIENT)
	public void render(PoseStack matrix, float partialTicks, int mouseX, int mouseY, Font fontRenderer, List<Component> tooltip){
		//Background
		FluidStack clientState = getStack();
//		Minecraft.getInstance().getTextureManager().bind(InventoryMenu.BLOCK_ATLAS);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

		Screen.fill(matrix, xPos + windowXStart, yPos + windowYStart - MAX_HEIGHT, xPos + windowXStart + 16, yPos + windowYStart, 0xFF959595);
		//Screen.fill changes the color
		RenderSystem.setShaderColor(1, 1, 1, 1);


		IClientFluidTypeExtensions attr = ((IClientFluidTypeExtensions)clientState.getFluid().getFluidType().getRenderPropertiesInternal()); 

		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(attr.getStillTexture());
		int col = attr.getTintColor(clientState);
		int height = (int) (MAX_HEIGHT * (float) clientState.getAmount() / (float) capacity);
		RenderSystem.setShaderColor((float) ((col >>> 16) & 0xFF) / 255F, ((float) ((col >>> 8) & 0xFF)) / 255F, ((float) (col & 0xFF)) / 255F, 1F);
		Screen.blit(matrix, xPos + windowXStart, yPos + windowYStart - height, 0, 16, height, sprite);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		//Foreground
//		Minecraft.getInstance().getTextureManager().bind(OVERLAY);
		RenderSystem.setShaderTexture(0, OVERLAY);
		Screen.blit(matrix, windowXStart + xPos, windowYStart + yPos - MAX_HEIGHT, 0, 0, 16, MAX_HEIGHT, 16, MAX_HEIGHT);

		if(mouseX >= xPos + windowXStart && mouseX <= xPos + windowXStart + 16 && mouseY >= yPos + windowYStart - MAX_HEIGHT && mouseY <= yPos + windowYStart){
			if(clientState.isEmpty()){
				tooltip.add(Component.translatable("tt.essentials.fluid_contents.empty"));
			}else{
				tooltip.add(clientState.getDisplayName());
			}
			tooltip.add(Component.translatable("tt.essentials.fluid_contents", clientState.getAmount(), capacity));
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
	public static Pair<Slot, Slot> createFluidSlots(Container inv, int startIndex, int inXPos, int inYPos, int outXPos, int outYPos, @Nullable IFluidSlotTE te, int[] fluidIndex){
		InSlot in = new InSlot(inv, startIndex + 1, inXPos, inYPos, startIndex, te, fluidIndex);
		OutSlot out = new OutSlot(inv, startIndex, outXPos, outYPos, in);
		return Pair.of(out, in);
	}

	private static class OutSlot extends Slot{

		private final InSlot inSlot;

		private OutSlot(Container inventoryIn, int index, int xPosition, int yPosition, InSlot inSlot){
			super(inventoryIn, index, xPosition, yPosition);
			this.inSlot = inSlot;
		}

		@Override
		public boolean mayPlace(ItemStack stack){
			return false;
		}

		@Override
		public void onTake(Player player, ItemStack stack){
			super.onTake(player, stack);
			inSlot.setChanged();
		}
	}

	private static class InSlot extends Slot{

		private final int outSlotIndex;
		@Nullable
		private final IFluidSlotTE te;
		//Array of all indices to interact with
		private final int[] fluidIndices;
		private boolean internalChange = false;//Used to prevent infinite recursive loops with onSlotChanged


		private InSlot(Container inventoryIn, int index, int xPosition, int yPosition, int outSlotIndex, @Nullable IFluidSlotTE te, int[] fluidIndices){
			super(inventoryIn, index, xPosition, yPosition);
			this.outSlotIndex = outSlotIndex;
			this.te = te;
			this.fluidIndices = fluidIndices;
		}

		@Override
		public boolean mayPlace(ItemStack stack){
			return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
		}

		@Override
		public void setChanged(){
			super.setChanged();

			ItemStack inSlot = getItem();

			if(!internalChange && te != null && mayPlace(inSlot)){
				internalChange = true;
				ItemStack outSlot = container.getItem(outSlotIndex);
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
									container.setItem(outSlotIndex, itemHandler.getContainer());
									inSlot.shrink(1);
									container.setItem(getSlotIndex(), inSlot);
									container.setChanged();

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
								container.setItem(outSlotIndex, itemHandler.getContainer());
								inSlot.shrink(1);
								container.setItem(getSlotIndex(), inSlot);

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
									container.setItem(outSlotIndex, outSlot);
									inSlot.shrink(1);
									container.setItem(getSlotIndex(), inSlot);
									container.setChanged();

									internalChange = false;
									return;
								}else{
									//Failed- revert the changes and continue
									inSlotCopy = inSlot.copy();
									inSlotCopy.setCount(1);
									opt = inSlotCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
									itemHandler = opt.orElseThrow(NullPointerException::new);
									container.setItem(getSlotIndex(), inSlot);
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
								container.setItem(outSlotIndex, outSlot);
								inSlot.shrink(1);
								container.setItem(getSlotIndex(), inSlot);

								internalChange = false;
								return;
							}else{
								//Failed- revert the changes and continue
								inSlotCopy = inSlot.copy();
								inSlotCopy.setCount(1);
								opt = inSlotCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
								itemHandler = opt.orElseThrow(NullPointerException::new);
								container.setItem(getSlotIndex(), inSlot);
								//no markDirty, as the final result is the same as the start state
							}
						}
					}

				}
				internalChange = false;
			}
		}
	}

	public static class FakeInventory implements Container{

		private final AbstractContainerMenu container;

		public FakeInventory(AbstractContainerMenu cont){
			container = cont;
		}

		private final ItemStack[] stacks = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

		@Override
		public int getContainerSize(){
			return 2;
		}

		@Override
		public boolean isEmpty(){
			return stacks[0].isEmpty() && stacks[1].isEmpty();
		}

		@Override
		public ItemStack getItem(int index){
			return stacks[index];
		}

		@Override
		public ItemStack removeItem(int index, int count){
			setChanged();
			return stacks[index].split(count);
		}

		@Override
		public ItemStack removeItemNoUpdate(int index){
			ItemStack stack = stacks[index];
			stacks[index] = ItemStack.EMPTY;
			setChanged();
			return stack;
		}

		@Override
		public void setItem(int index, ItemStack stack){
			stacks[index] = stack;
			setChanged();
		}

		@Override
		public int getMaxStackSize(){
			return 64;
		}

		@Override
		public void setChanged(){
			container.broadcastChanges();
		}

		@Override
		public boolean stillValid(Player player){
			return true;
		}

		@Override
		public boolean canPlaceItem(int index, ItemStack stack){
			return index == 0 && stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
		}

		@Override
		public void clearContent(){
			stacks[0] = ItemStack.EMPTY;
			stacks[1] = ItemStack.EMPTY;
			setChanged();
		}
	}
}
