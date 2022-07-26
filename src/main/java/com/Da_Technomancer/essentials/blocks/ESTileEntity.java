package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.*;
import com.mojang.datafixers.DSL;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;

public class ESTileEntity{

	public static void init(){
		toRegister.put("brazier", BrazierTileEntity.TYPE);
		toRegister.put("slotted_chest", SlottedChestTileEntity.TYPE);
		toRegister.put("sorting_hopper", SortingHopperTileEntity.TYPE);
		toRegister.put("speed_hopper", SpeedHopperTileEntity.TYPE);
		toRegister.put("item_shifter", ItemShifterTileEntity.TYPE);
		toRegister.put("fluid_shifter", FluidShifterTileEntity.TYPE);
		toRegister.put("hopper_filter", HopperFilterTileEntity.TYPE);
		toRegister.put("basic_item_splitter", BasicItemSplitterTileEntity.TYPE);
		toRegister.put("item_splitter", ItemSplitterTileEntity.TYPE);
		toRegister.put("basic_fluid_splitter", BasicFluidSplitterTileEntity.TYPE);
		toRegister.put("fluid_splitter", FluidSplitterTileEntity.TYPE);
		toRegister.put("circuit", CircuitTileEntity.TYPE);
		toRegister.put("cons_circuit", ConstantCircuitTileEntity.TYPE);
		toRegister.put("timer_circuit", TimerCircuitTileEntity.TYPE);
		toRegister.put("delay_circuit", DelayCircuitTileEntity.TYPE);
		toRegister.put("wire", WireTileEntity.TYPE);
		toRegister.put("wire_junction", WireJunctionTileEntity.TYPE);
		toRegister.put("auto_crafter", AutoCrafterTileEntity.TYPE);
		toRegister.put("redstone_transmitter", RedstoneTransmitterTileEntity.TYPE);
		toRegister.put("redstone_receiver", RedstoneReceiverTileEntity.TYPE);
		toRegister.put("pulse_circuit", PulseCircuitTileEntity.TYPE);
		toRegister.put("d_counter_circuit", DCounterCircuitTileEntity.TYPE);
		toRegister.put("interface_circuit", InterfaceCircuitTileEntity.TYPE);
	}

	public static final HashMap<String, BlockEntityType<?>> toRegister = new HashMap<>();

	public static <T extends BlockEntity> BlockEntityType<T> createType(BlockEntityType.BlockEntitySupplier<T> cons, Block... blocks){
		return BlockEntityType.Builder.of(cons, blocks).build(DSL.emptyPartType());
	}
}
