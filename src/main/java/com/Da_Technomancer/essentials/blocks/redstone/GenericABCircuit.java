package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

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
		super(name +"_circuit");
		this.ttName = "tt." + Essentials.MODID + "." + name + "_circuit";
		this.function = function;
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or != CircuitTileEntity.Orient.FRONT;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		return function.apply(Math.max(in0, in2), in1);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent(ttName));
	}
}
