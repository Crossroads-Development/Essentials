package com.Da_Technomancer.essentials.gui.container;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public abstract class CircuitContainer extends AbstractContainerMenu{

	public final BlockPos pos;
	public final String[] inputs = new String[inputBars()];

	protected CircuitContainer(MenuType<? extends CircuitContainer> type, int id, Inventory playerInventory, FriendlyByteBuf data){
		super(type, id);
		if(data == null){
			pos = null;
		}else{
			pos = data.readBlockPos();
			for(int i = 0; i < inputs.length; i++){
				inputs[i] = data.readUtf(Short.MAX_VALUE);
			}
		}
	}

	public static FriendlyByteBuf createEmptyBuf(){
		return new FriendlyByteBuf(Unpooled.buffer());
	}

	public static FriendlyByteBuf encodeData(FriendlyByteBuf buf, BlockPos pos, String... inputs){
		buf.writeBlockPos(pos);
		for(String input : inputs){
			buf.writeUtf(input);
		}
		return buf;
	}

	@Override
	public boolean stillValid(Player playerIn){
		return pos != null && pos.distToCenterSqr(playerIn.position()) <= 64;
	}

	public abstract int inputBars();

	@Override
	public ItemStack quickMoveStack(Player playerIn, int fromSlot){
		return ItemStack.EMPTY;//No-op
	}
}
