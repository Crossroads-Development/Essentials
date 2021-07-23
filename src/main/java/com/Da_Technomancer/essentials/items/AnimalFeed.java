package com.Da_Technomancer.essentials.items;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class AnimalFeed extends Item{

	protected AnimalFeed(){
		super(new Properties().tab(ESItems.TAB_ESSENTIALS));
		String name = "animal_feed";
		setRegistryName(name);
		ESItems.toRegister.add(this);
		DispenserBlock.registerBehavior(this, new Dispense());
	}

	private static class Dispense extends OptionalDispenseBehavior{

		@Override
		protected ItemStack execute(IBlockSource source, ItemStack stack){
			Level world = source.getLevel();
			if(!world.isClientSide()){
				setSuccess(false);
				BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));

				for(AnimalEntity e : world.getEntitiesOfClass(AnimalEntity.class, new AxisAlignedBB(blockpos))){
					if(!stack.isEmpty() && e.getAge() == 0 && (!(e instanceof TameableEntity) || ((TameableEntity) e).isTame())){
						e.setInLove(null);
						stack.shrink(1);
						setSuccess(true);
					}
				}
			}

			return stack;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.animal_feed"));
	}
}
