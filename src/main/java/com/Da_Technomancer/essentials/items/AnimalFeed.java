package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
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
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class AnimalFeed extends Item{

	protected AnimalFeed(){
		super(new Properties().group(EssentialsItems.TAB_ESSENTIALS));
		String name = "animal_feed";
		setRegistryName(name);
		EssentialsItems.toRegister.add(this);
		DispenserBlock.registerDispenseBehavior(this, new Dispense());
	}

	private class Dispense extends OptionalDispenseBehavior{

		@Override
		protected ItemStack dispenseStack(IBlockSource source, ItemStack stack){
			World world = source.getWorld();
			if(!world.isRemote()){
				this.successful = false;
				BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));

				for(AnimalEntity e : world.getEntitiesWithinAABB(AnimalEntity.class, new AxisAlignedBB(blockpos))){
					if(!stack.isEmpty() && e.getGrowingAge() == 0 && e.canBreed() && (!(e instanceof TameableEntity) || ((TameableEntity) e).isTamed())){
						e.setInLove(null);
						stack.shrink(1);
						successful = true;
					}
				}
			}

			return stack;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt." + Essentials.MODID + ".animal_feed"));
	}
}
