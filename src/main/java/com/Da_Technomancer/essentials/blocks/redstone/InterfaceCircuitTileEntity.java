package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.interfaceCircuit;

public class InterfaceCircuitTileEntity extends CircuitTileEntity{

	public static final BlockEntityType<InterfaceCircuitTileEntity> TYPE = ESTileEntity.createType(InterfaceCircuitTileEntity::new, interfaceCircuit);

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
