package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.tileentities.FluidShifterTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.OnlyIn;

public class FluidShifterContainer extends Container{

	public final IInventory playerInv;
	public final FluidShifterTileEntity te;
	private final int[] fields = new int[2];
	public static final int[] invStart = {8, 84};

	public FluidShifterContainer(IInventory playerInv, FluidShifterTileEntity te){
		this.playerInv = playerInv;
		this.te = te;
		addSlotToContainer(new FluidSlot(this, 100, 19, 100, 54));

		//Hotbar
		for(int x = 0; x < 9; ++x){
			addSlotToContainer(new Slot(playerInv, x, invStart[0] + x * 18, invStart[1] + 58));
		}

		//Main player inv
		for(int y = 0; y < 3; ++y){
			for(int x = 0; x < 9; ++x){
				addSlotToContainer(new Slot(playerInv, x + y * 9 + 9, invStart[0] + x * 18, invStart[1] + y * 18));
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot){
		ItemStack previous = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(fromSlot);

		if(slot != null && slot.getHasStack()){
			ItemStack current = slot.getStack();
			previous = current.copy();

			//fromSlot < slotCount means TE -> Player, else Player -> TE input slots
			if(fromSlot < 2 ? !mergeItemStack(current, 2, 36 + 2, true) : !mergeItemStack(current, 0, 2, false)){
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
	public boolean canInteractWith(EntityPlayer playerIn){
		return te.isUsableByPlayer(playerIn);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void updateProgressBar(int id, int data){
		te.setField(id, data);
	}

	@Override
	public void addListener(IContainerListener listener){
		super.addListener(listener);
		listener.sendAllWindowProperties(this, te);
	}

	@Override
	public void detectAndSendChanges(){
		super.detectAndSendChanges();

		for(int i = 0; i < fields.length; i++){
			if(fields[i] != te.getField(i)){
				fields[i] = te.getField(i);
				for(IContainerListener listener : listeners){
					listener.sendWindowProperty(this, i, fields[i]);
				}
			}
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn){
		super.onContainerClosed(playerIn);


		if(!te.getWorld().isRemote){
			if(playerIn.isEntityAlive() && !(playerIn instanceof EntityPlayerMP && ((EntityPlayerMP) playerIn).hasDisconnected())){
				playerIn.inventory.placeItemBackInInventory(te.getWorld(), inventorySlots.get(0).getStack());
				playerIn.inventory.placeItemBackInInventory(te.getWorld(), inventorySlots.get(1).getStack());
			}else{
				playerIn.dropItem(inventorySlots.get(0).getStack(), false);
				playerIn.dropItem(inventorySlots.get(1).getStack(), false);
			}
		}
	}

	protected static class FluidSlot extends Slot{

		protected final FluidShifterContainer cont;
		protected final Slot outputSlot;

		/**
		 * Note that this constructor will also initialize an output slot and add it to the container, meaning each FluidSlot added occupies two slots.
		 *
		 * @param container The containing container
		 * @param x The x position of this slot
		 * @param y The y position of this slot
		 * @param outputX The x position of the output slot
		 * @param outputY The y position of the output slot
		 */
		public FluidSlot(FluidShifterContainer container, int x, int y, int outputX, int outputY){
			super(new FakeInventory(), 0, x, y);
			this.cont = container;
			outputSlot = new Slot(inventory, 1, outputX, outputY){
				@Override
				public boolean isItemValid(ItemStack stack){
					return false;
				}

				@Override
				public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack){
					FluidSlot.this.onSlotChanged();
					return stack;
				}
			};
			container.addSlotToContainer(outputSlot);
		}

		@Override
		public boolean isItemValid(ItemStack stack){
			return FluidUtil.getFluidHandler(stack) != null;
		}

		@Override
		public void onSlotChanged(){
			if(!cont.te.getWorld().isRemote){
				cont.detectAndSendChanges();

				ItemStack stack = getStack().copy();
				if(!stack.isEmpty()){
					stack.setCount(1);
					IFluidHandler teHandler = cont.te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
					IFluidHandlerItem stackHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);

					if(teHandler != null && stackHandler != null){
						FluidStack stFs = stackHandler.drain(Integer.MAX_VALUE, false);
						FluidStack teFs = teHandler.drain(Integer.MAX_VALUE, false);
						ItemStack outputStack = inventory.getStackInSlot(1);
						if(stFs == null && teFs != null){
							//Try filling item
							int filled = stackHandler.fill(teFs, true);
							ItemStack container = stackHandler.getContainer();//The container is only updated if we actually do the fill. This is dumb, but there's no way around it
							if(filled > 0 && (outputStack.isEmpty() || ItemStack.areItemsEqual(outputStack, container) && ItemStack.areItemStackTagsEqual(outputStack, container) && outputStack.getCount() < outputStack.getMaxStackSize())){
								teHandler.drain(filled, true);

								if(outputStack.isEmpty()){
									outputStack = container;
								}else{
									outputStack.grow(container.getCount());
								}

								inventory.setInventorySlotContents(1, outputStack);
								inventory.decrStackSize(0, 1);
								cont.detectAndSendChanges();
							}
						}else if(stFs != null){
							//Try draining item
							int drained = teHandler.fill(stFs, false);
							FluidStack drainedFs = stackHandler.drain(drained, true);
							if(drained == 0 || drainedFs == null || drained != drainedFs.amount){
								return;//Something has gone weird, and a checksum failed. May be caused by, for example, buckets with a minimum drain qty
							}
							ItemStack container = stackHandler.getContainer();//The container is only updated if we actually do the drain. This is dumb, but there's no way around it
							if((outputStack.isEmpty() || ItemStack.areItemsEqual(outputStack, container) && ItemStack.areItemStackTagsEqual(outputStack, container) && outputStack.getCount() < outputStack.getMaxStackSize())){
								teHandler.fill(drainedFs, true);

								if(outputStack.isEmpty()){
									outputStack = container;
								}else{
									outputStack.grow(container.getCount());
								}

								inventory.setInventorySlotContents(1, outputStack);
								inventory.decrStackSize(0, 1);
								cont.detectAndSendChanges();
							}
						}
					}
				}
			}
		}

		private static class FakeInventory implements IInventory{

			protected final ItemStack[] stacks = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY};

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
				return stacks[index].split(count);
			}

			@Override
			public ItemStack removeStackFromSlot(int index){
				ItemStack stack = stacks[index];
				stacks[index] = ItemStack.EMPTY;
				return stack;
			}

			@Override
			public void setInventorySlotContents(int index, ItemStack stack){
				stacks[index] = stack;
			}

			@Override
			public int getInventoryStackLimit(){
				return 64;
			}

			@Override
			public void markDirty(){

			}

			@Override
			public boolean isUsableByPlayer(EntityPlayer player){
				return true;
			}

			@Override
			public void openInventory(EntityPlayer player){

			}

			@Override
			public void closeInventory(EntityPlayer player){

			}

			@Override
			public boolean isItemValidForSlot(int index, ItemStack stack){
				return index == 0 && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			}

			@Override
			public int getField(int id){
				return 0;
			}

			@Override
			public void setField(int id, int value){

			}

			@Override
			public int getFieldCount(){
				return 0;
			}

			@Override
			public void clear(){
				stacks[0] = ItemStack.EMPTY;
				stacks[1] = ItemStack.EMPTY;
			}

			@Override
			public String getName(){
				return "";
			}

			@Override
			public boolean hasCustomName(){
				return false;
			}

			@Override
			public ITextComponent getDisplayName(){
				return new TextComponentString("");
			}
		}
	}
}
