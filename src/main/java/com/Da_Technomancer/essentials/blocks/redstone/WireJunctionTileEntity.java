package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.wireJunctionCircuit;

public class WireJunctionTileEntity extends GenericWireBypassTileEntity{

	public static final BlockEntityType<WireJunctionTileEntity> TYPE = ESTileEntity.createType(WireJunctionTileEntity::new, wireJunctionCircuit);

	public WireJunctionTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	protected GenericWireBypass getBlock(){
		return wireJunctionCircuit;
	}
}
