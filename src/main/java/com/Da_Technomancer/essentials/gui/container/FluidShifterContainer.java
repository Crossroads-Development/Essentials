package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.FluidSlotManager;
import com.Da_Technomancer.essentials.api.IntDeferredRef;
import com.Da_Technomancer.essentials.blocks.FluidShifterTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

public class FluidShifterContainer extends AbstractContainerMenu{

	@ObjectHolder(registryName="menu", value=Essentials.MODID + ":fluid_shifter")
	private static MenuType<FluidShifterContainer> TYPE = null;

	private final Container inv;
	private final BlockPos pos;
	public final FluidShifterTileEntity te;

	public final IntDeferredRef fluidIdRef;
	public final IntDeferredRef fluidQtyRef;

	public FluidShifterContainer(int id, Inventory playerInventory, FriendlyByteBuf data){
		this(id, playerInventory, data.readBlockPos());
	}

	public FluidShifterContainer(int id, Inventory playerInventory, BlockPos pos){
		super(TYPE, id);
		this.inv = new FluidSlotManager.FakeInventory(this);
		this.pos = pos;
		BlockEntity t = playerInventory.player.level.getBlockEntity(pos);
		if(t instanceof FluidShifterTileEntity){
			this.te = (FluidShifterTileEntity) t;
			//Track fluid fields
			boolean remote = te.getLevel().isClientSide;
			fluidIdRef = new IntDeferredRef(te.getFluidManager()::getFluidId, remote);
			fluidQtyRef = new IntDeferredRef(te.getFluidManager()::getFluidQty, remote);
			addDataSlot(fluidIdRef);
			addDataSlot(fluidQtyRef);
		}else{
			this.te = null;
			fluidIdRef = null;
			fluidQtyRef = null;
		}
		Pair<Slot, Slot> slots = FluidSlotManager.createFluidSlots(inv, 0, 100, 19, 100, 54, this.te, new int[] {0});
		addSlot(slots.getLeft());
		Slot fluidInSlot = slots.getRight();
		te.getFluidManager().linkSlot(fluidInSlot);
		addSlot(fluidInSlot);

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
	public ItemStack quickMoveStack(Player playerIn, int fromSlot){
		ItemStack previous = ItemStack.EMPTY;
		Slot slot = slots.get(fromSlot);

		if(slot != null && slot.hasItem()){
			ItemStack current = slot.getItem();
			previous = current.copy();

			//fromSlot < slotCount means TE -> Player, else Player -> TE input slots
			if(fromSlot < 2 ? !moveItemStackTo(current, 2, 36 + 2, true) : !moveItemStackTo(current, 0, 2, false)){
				return ItemStack.EMPTY;
			}

			if(current.isEmpty()){
				slot.set(ItemStack.EMPTY);
			}else{
				slot.setChanged();
			}

			if(current.getCount() == previous.getCount()){
				return ItemStack.EMPTY;
			}
			slot.onTake(playerIn, current);
		}

		return previous;
	}

	@Override
	public boolean stillValid(Player playerIn){
		return pos.distToCenterSqr(playerIn.position()) <= 64;
	}

	@Override
	public void removed(Player playerIn){
		super.removed(playerIn);

		if(te != null && !te.getLevel().isClientSide){
			if(playerIn.isAlive() && !(playerIn instanceof ServerPlayer && ((ServerPlayer) playerIn).hasDisconnected())){
				playerIn.getInventory().placeItemBackInInventory(inv.getItem(0));
				playerIn.getInventory().placeItemBackInInventory(inv.getItem(1));
			}else{
				playerIn.drop(slots.get(0).getItem(), false);
				playerIn.drop(slots.get(1).getItem(), false);
			}
		}
	}
}
