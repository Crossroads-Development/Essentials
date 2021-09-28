package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.DCounterCircuitTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class DCounterCircuit extends AbstractCircuit{

	public DCounterCircuit(){
		super("d_counter_circuit");
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or != CircuitTileEntity.Orient.FRONT;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		if(te instanceof DCounterCircuitTileEntity){
			return ((DCounterCircuitTileEntity) te).calculateNewOutputAndUpdate(in0, in1, in2);//This is calculated by the TE for this block
		}

		return 0;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new DCounterCircuitTileEntity(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tt.essentials.d_counter_circuit"));
	}
}
