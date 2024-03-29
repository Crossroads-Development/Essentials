package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class GenericABCircuit extends AbstractCircuit{

	private final String ttName;
	private final BiFunction<Float, Float, Float> function;

	/**
	 * Creates a circuit to perform a pure state-based operation, with 2 distinct inputs and 1 output
	 * @param name The name of this circuit
	 * @param function The operation function- Float 1 is side input, Float 2 is back input. Output is sanitized
	 */
	public GenericABCircuit(String name, BiFunction<Float, Float, Float> function){
		super(name + "_circuit");
		this.ttName = "tt." + Essentials.MODID + "." + name + "_circuit";
		this.function = function;
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or != CircuitTileEntity.Orient.FRONT;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		return function.apply(RedstoneUtil.chooseInput(in0, in2), in1);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable(ttName));
	}
}
