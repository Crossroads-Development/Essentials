package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;

public class InterfaceCircuitTileEntity extends CircuitTileEntity{

	@ObjectHolder(registryName="block_entity_type", value=Essentials.MODID + ":interface_circuit")
	private static BlockEntityType<ConstantCircuitTileEntity> TYPE = null;

	public InterfaceCircuitTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	/*
	 * This block having a custom tile entity is specifically to implement computercraft support,
	 * so that computercraft can set an output value that overrides normal circuit behavior
	 */

	public Float externalInput = null;

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.interfaceCircuit;
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		if(externalInput != null){
			nbt.putFloat("external_input", externalInput);
		}else{
			nbt.remove("external_input");
		}
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		if(nbt.contains("external_input")){
			externalInput = nbt.getFloat("external_input");
		}else{
			externalInput = null;
		}
	}

	@Override
	public void setBlockState(BlockState state){
		externalInput = null;
		super.setBlockState(state);
	}
}
