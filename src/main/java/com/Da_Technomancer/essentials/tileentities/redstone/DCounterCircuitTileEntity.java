package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Essentials.MODID)
public class DCounterCircuitTileEntity extends CircuitTileEntity{

	@ObjectHolder("d_counter_circuit")
	public static BlockEntityType<DCounterCircuitTileEntity> TYPE = null;

	private float counter = 0;
	private float prevInputBack = 0F;
	private boolean hadInputSideL = false;
	private boolean hadInputSideR = false;

	public DCounterCircuitTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	public float calculateNewOutputAndUpdate(float in0, float in1, float in2){
		//We only respond to the reset when it is a pulse- which means it was previously 0
		if((in0 > 0) != hadInputSideL || (in2 > 0) != hadInputSideR){
			hadInputSideL = in0 > 0;
			hadInputSideR = in2 > 0;
			counter = 0;
			prevInputBack = in1;
			setChanged();
			return counter;
		}

		if(RedstoneUtil.didChange(prevInputBack, in1)){
			counter += in1;
			prevInputBack = in1;
			setChanged();
		}
		return counter;
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		nbt.putFloat("counter", counter);
		nbt.putFloat("input_back", prevInputBack);
		nbt.putBoolean("input_left", hadInputSideL);
		nbt.putBoolean("input_right", hadInputSideR);
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		counter = nbt.getFloat("counter");
		prevInputBack = nbt.getFloat("input_back");
		hadInputSideL = nbt.getBoolean("input_left");
		hadInputSideR = nbt.getBoolean("input_right");
	}
}
