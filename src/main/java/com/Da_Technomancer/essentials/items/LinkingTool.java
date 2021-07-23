package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.Player;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.ILevelReader;
import net.minecraft.world.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LinkingTool extends Item{

	public LinkingTool(){
		super(new Item.Properties().stacksTo(1).tab(ESItems.TAB_ESSENTIALS));
		String name = "linking_tool";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}


	@Override
	public boolean doesSneakBypassUse(ItemStack stack, ILevelReader world, BlockPos pos, Player player){
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		CompoundNBT nbt = stack.getTag();
		if(nbt != null && nbt.contains(LinkHelper.POS_NBT)){
			BlockPos linked = BlockPos.of(stack.getTag().getLong(LinkHelper.POS_NBT));
			String dim = stack.getTag().getString(LinkHelper.DIM_NBT);
			tooltip.add(new TranslationTextComponent("tt.essentials.linking.info", linked.getX(), linked.getY(), linked.getZ(), dim));
		}else{
			tooltip.add(new TranslationTextComponent("tt.essentials.linking.none"));
		}
		tooltip.add(new TranslationTextComponent("tt.essentials.linking.desc"));
	}
}
