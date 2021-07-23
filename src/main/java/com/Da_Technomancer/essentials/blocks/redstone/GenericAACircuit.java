package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitBlockEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class GenericAACircuit extends AbstractCircuit{

	private final String ttName;
	private final BiFunction<Float, Float, Float> function;

	public GenericAACircuit(String name, BiFunction<Float, Float, Float> function){
		super(name +"_circuit");
		this.ttName = "tt." + Essentials.MODID + "." + name + "_circuit";
		this.function = function;
	}

	@Override
	public boolean useInput(CircuitBlockEntity.Orient or){
		return or == CircuitBlockEntity.Orient.CCW || or == CircuitBlockEntity.Orient.CW;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitBlockEntity te){
		return function.apply(in0, in2);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent(ttName));
	}
}
