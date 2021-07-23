package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;

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
	public boolean useInput(CircuitTileEntity.Orient or){
		return or == CircuitTileEntity.Orient.CCW || or == CircuitTileEntity.Orient.CW;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		return function.apply(in0, in2);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent(ttName));
	}
}
