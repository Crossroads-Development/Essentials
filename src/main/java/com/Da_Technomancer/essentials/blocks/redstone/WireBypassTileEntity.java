package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.wireBypassCircuit;

public class WireBypassTileEntity extends GenericWireBypassTileEntity{

	public static final BlockEntityType<WireBypassTileEntity> TYPE = ESTileEntity.createType(WireBypassTileEntity::new, wireBypassCircuit);

	public WireBypassTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	protected GenericWireBypass getBlock(){
		return wireBypassCircuit;
	}
}
