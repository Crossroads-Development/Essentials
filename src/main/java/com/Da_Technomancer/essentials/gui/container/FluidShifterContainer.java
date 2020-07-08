package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.FluidShifterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

@ObjectHolder(Essentials.MODID)
public class FluidShifterContainer extends Container{

	@ObjectHolder("fluid_shifter")
	private static ContainerType<FluidShifterContainer> TYPE = null;

	private final IInventory inv;
	private final BlockPos pos;
	public final FluidShifterTileEntity te;

	public final IntDeferredRef fluidIdRef;
	public final IntDeferredRef fluidQtyRef;

	public FluidShifterContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		this(id, playerInventory, data.readBlockPos());
	}

	public FluidShifterContainer(int id, PlayerInventory playerInventory, BlockPos pos){
		super(TYPE, id);
		this.inv = new FluidSlotManager.FakeInventory(this);
		this.pos = pos;
		TileEntity t = playerInventory.player.world.getTileEntity(pos);
		if(t instanceof FluidShifterTileEntity){
			this.te = (FluidShifterTileEntity) t;
			//Track fluid fields
			boolean remote = te.getWorld().isRemote;
			fluidIdRef = new IntDeferredRef(te.getFluidManager()::getFluidId, remote);
			fluidQtyRef = new IntDeferredRef(te.getFluidManager()::getFluidQty, remote);
			trackInt(fluidIdRef);
			trackInt(fluidQtyRef);
		}else{
			this.te = null;
			fluidIdRef = null;
			fluidQtyRef = null;
		}
		Pair<Slot, Slot> slots = FluidSlotManager.createFluidSlots(inv, 0, 100, 19, 100, 54, this.te, new int[] {0});
		addSlot(slots.getLeft());
		addSlot(slots.getRight());

		//Player inv
		for(int i = 0; i < 9; i++){
			addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
		}

		for(int i = 0; i < 3; i++){
			for(int j = 0; j < 9; j++){
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot){
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
	public boolean canInteractWith(PlayerEntity playerIn){
		return pos.distanceSq(playerIn.getPositionVec(), true) <= 64;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn){
		super.onContainerClosed(playerIn);

		if(te != null && !te.getWorld().isRemote){
			if(playerIn.isAlive() && !(playerIn instanceof ServerPlayerEntity && ((ServerPlayerEntity) playerIn).hasDisconnected())){
				playerIn.inventory.placeItemBackInInventory(te.getWorld(), inv.getStackInSlot(0).getStack());
				playerIn.inventory.placeItemBackInInventory(te.getWorld(), inv.getStackInSlot(1).getStack());
			}else{
				playerIn.dropItem(inventorySlots.get(0).getStack(), false);
				playerIn.dropItem(inventorySlots.get(1).getStack(), false);
			}
		}
	}
}
