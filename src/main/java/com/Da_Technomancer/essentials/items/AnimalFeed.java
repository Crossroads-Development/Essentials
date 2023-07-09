package com.Da_Technomancer.essentials.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class AnimalFeed extends Item{

	protected AnimalFeed(){
		super(ESItems.baseItemProperties());
		String name = "animal_feed";
		ESItems.queueForRegister(name, this);
		DispenserBlock.registerBehavior(this, new Dispense());
	}

	private static class Dispense extends OptionalDispenseItemBehavior{

		@Override
		protected ItemStack execute(BlockSource source, ItemStack stack){
			Level world = source.getLevel();
			if(!world.isClientSide()){
				setSuccess(false);
				BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));

				for(Animal e : world.getEntitiesOfClass(Animal.class, new AABB(blockpos))){
					if(!stack.isEmpty() && e.getAge() == 0 && (!(e instanceof TamableAnimal) || ((TamableAnimal) e).isTame())){
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
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.animal_feed"));
	}
}
