package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.packets.SendNBTToClient;
import com.mojang.blaze3d.platform.GlStateManager;
import jdk.internal.jline.internal.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class FluidSlotManager{

	//General
	private final World world;
	private final BlockPos pos;
	private FluidStack prevSyncedState;
	private final int capacity;
	private final int key;

	//Per screen
	private int windowXStart;
	private int windowYStart;

	private static final int MAX_HEIGHT = 48;
	private static final ResourceLocation OVERLAY = new ResourceLocation(Essentials.MODID, "textures/gui/rectangle_fluid_overlay.png");

	public FluidSlotManager(World world, BlockPos pos, FluidStack init, int capacity, int key){
		this.world = world;
		this.pos = pos;
		prevSyncedState = init.copy();
		this.capacity = capacity;
		this.key = key;
	}

	@OnlyIn(Dist.CLIENT)
	public void initScreen(int windowXStart, int windowYStart){
		this.windowXStart = windowXStart;
		this.windowYStart = windowYStart;
	}

	public boolean updateState(FluidStack newFluid){
		if(BlockUtil.sameFluid(prevSyncedState, newFluid) && newFluid.getAmount() == prevSyncedState.getAmount()){
			return false;
		}
		prevSyncedState = newFluid.copy();

		markChanged();
		return true;
	}

	public FluidStack getStack(){
		return prevSyncedState;
	}

	public void markChanged(){
		if(!world.isRemote){
			CompoundNBT nbt = new CompoundNBT();
			nbt.putBoolean("fluid_" + key, true);
			prevSyncedState.writeToNBT(nbt);
			BlockUtil.sendClientPacketAround(world, pos, new SendNBTToClient(nbt, pos));
		}
	}

	public boolean handlePacket(CompoundNBT nbt){
		if(nbt.getBoolean("fluid_" + key)){
			prevSyncedState = FluidStack.loadFluidStackFromNBT(nbt);
			return true;
		}

		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void renderBack(int x, int y, float partialTicks, int mouseX, int mouseY, FontRenderer fontRenderer){
		Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

		Screen.fill(x + windowXStart, y + windowYStart - MAX_HEIGHT, x + windowXStart + 16, y + windowYStart, 0xFF959595);
		//Screen.fill changes the color
		GlStateManager.color4f(1, 1, 1, 1);

		FluidAttributes attr = prevSyncedState.getFluid().getAttributes();

		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureMap().getSprite(attr.getStillTexture());
		//Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		int col = attr.getColor(prevSyncedState);
		int height = (int) (MAX_HEIGHT * (float) prevSyncedState.getAmount() / (float) capacity);
		GlStateManager.color4f((float) ((col >>> 16) & 0xFF) / 255F, ((float) ((col >>> 8) & 0xFF)) / 255F, ((float) (col & 0xFF)) / 255F, ((float) ((col >>> 24) & 0xFF)) / 255F);
		Screen.blit(x + windowXStart, y + windowYStart - height, 0, 16, height, sprite);
		GlStateManager.color3f(1, 1, 1);
	}

	@OnlyIn(Dist.CLIENT)
	public void renderFore(int x, int y, int mouseX, int mouseY, FontRenderer fontRenderer, List<String> tooltip){
		Minecraft.getInstance().getTextureManager().bindTexture(OVERLAY);
		Screen.blit(x, y - MAX_HEIGHT, 0, 0, 16, MAX_HEIGHT, 16, MAX_HEIGHT);

		if(mouseX >= x + windowXStart && mouseX <= x + windowXStart + 16 && mouseY >= y + windowYStart - MAX_HEIGHT && mouseY <= y + windowYStart){
			if(prevSyncedState.isEmpty()){
				tooltip.add(new TranslationTextComponent("tt.essentials.empty_fluid").getFormattedText());
			}else{
				tooltip.add(prevSyncedState.getDisplayName().getFormattedText());
				tooltip.add(prevSyncedState.getAmount() + "/" + capacity);
			}
		}
	}

	public static Pair<Slot, Slot> createFluidSlots(Container cont, IInventory inv, int startIndex, int inXPos, int inYPos, int outXPos, int outYPos, @Nullable IFluidSlotTE te, int[] fluidIndex){
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
				IFluidHandlerItem itHandler;
				IFluidHandler handler = te.getFluidHandler();
				if((itHandler = opt.orElse(null)) != null){
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
}
