package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class LinkingTool extends Item{

	public LinkingTool(){
		super(new Item.Properties().maxStackSize(1).group(ESItems.TAB_ESSENTIALS));
		String name = "linking_tool";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}


	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player){
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		CompoundNBT nbt = stack.getTag();
		if(nbt != null && nbt.contains(ILinkTE.POS_NBT)){
			BlockPos linked = BlockPos.fromLong(stack.getTag().getLong(ILinkTE.POS_NBT));
			String dim = stack.getTag().getString(ILinkTE.DIM_NBT);
			tooltip.add(new TranslationTextComponent("tt.essentials.linking.info", linked.getX(), linked.getY(), linked.getZ(), dim));
		}else{
			tooltip.add(new TranslationTextComponent("tt.essentials.linking.none"));
		}
		tooltip.add(new TranslationTextComponent("tt.essentials.linking.desc"));
	}
}
