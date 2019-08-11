package com.Da_Technomancer.essentials.gui.container;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class ConstantCircuitContainer extends Container{

	public float output;
	public String conf;
	public BlockPos pos;

	@ObjectHolder("cons_circuit")
	private static ContainerType<ConstantCircuitContainer> TYPE = null;

	public ConstantCircuitContainer(int id, PlayerInventory playerInventory, PacketBuffer data){
		this(id, playerInventory, data == null ? 0 : data.readFloat(), data == null ? null : data.readString(), data == null ? null : data.readBlockPos());
	}

	public ConstantCircuitContainer(int id, PlayerInventory playerInventory, float output, String settingStr, BlockPos pos){
		super(TYPE, id);
		this.output = output;
		this.conf = settingStr;
		this.pos = pos;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn){
		return true;
	}
}
