package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

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
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player){
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		CompoundTag nbt = stack.getTag();
		if(nbt != null && nbt.contains(LinkHelper.POS_NBT)){
			BlockPos linked = BlockPos.of(stack.getTag().getLong(LinkHelper.POS_NBT));
			String dim = stack.getTag().getString(LinkHelper.DIM_NBT);
			tooltip.add(new TranslatableComponent("tt.essentials.linking.info", linked.getX(), linked.getY(), linked.getZ(), dim));
		}else{
			tooltip.add(new TranslatableComponent("tt.essentials.linking.none"));
		}
		tooltip.add(new TranslatableComponent("tt.essentials.linking.desc"));
	}
}
