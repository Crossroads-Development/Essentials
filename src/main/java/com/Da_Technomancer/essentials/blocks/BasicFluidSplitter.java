package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.AbstractSplitterTE;
import com.Da_Technomancer.essentials.tileentities.BasicFluidSplitterTileEntity;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BasicFluidSplitter extends AbstractSplitter{

	protected BasicFluidSplitter(String name, Properties prop){
		super(name, prop);
	}

	public BasicFluidSplitter(){
		super("basic_fluid_splitter", Properties.of(Material.METAL).strength(3));
	}

	@Override
	protected boolean isBasic(){
		return true;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new BasicFluidSplitterTileEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.fluid_splitter_basic"));
		tooltip.add(new TranslatableComponent("tt.essentials.basic_fluid_splitter_formula"));
		tooltip.add(new TranslatableComponent("tt.essentials.fluid_splitter_chute"));
	}

	@Override
	protected Component getModeComponent(AbstractSplitterTE te, int newMode){
		return new TranslatableComponent("tt.essentials.basic_fluid_splitter.mode", newMode, te.getDistribution().base);
	}
}
