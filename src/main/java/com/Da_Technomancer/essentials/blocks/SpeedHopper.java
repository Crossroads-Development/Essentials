package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.SpeedHopperBlockEntity;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.AbstractBlock.Properties;

public class SpeedHopper extends SortingHopper{

	protected SpeedHopper(){
		super(Properties.of(Material.METAL).sound(SoundType.METAL).strength(2));
		String name = "speed_hopper";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public BlockEntity newBlockEntity(IBlockReader world){
		return new SpeedHopperBlockEntity();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.speed_hopper.sort"));
		tooltip.add(new TranslationTextComponent("tt.essentials.speed_hopper.desc"));
	}
}
