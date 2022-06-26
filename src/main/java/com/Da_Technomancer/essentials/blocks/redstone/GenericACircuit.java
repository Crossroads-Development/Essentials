package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class GenericACircuit extends AbstractCircuit{

	private final String ttName;
	private final Function<Float, Float> function;
	private final boolean usesQuartz;

	public GenericACircuit(String name, Function<Float, Float> function){
		this(name, function, true);
	}

	public GenericACircuit(String name, Function<Float, Float> function, boolean usesQuartz){
		super(name + "_circuit");
		this.ttName = "tt." + Essentials.MODID + "." + name + "_circuit";
		this.function = function;
		this.usesQuartz = usesQuartz;
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or == CircuitTileEntity.Orient.BACK;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		return function.apply(in1);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable(ttName));
	}

	@Override
	public boolean usesQuartz(){
		return usesQuartz;
	}
}
