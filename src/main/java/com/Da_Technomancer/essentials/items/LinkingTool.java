package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.api.LinkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nullable;
import java.util.List;

public class LinkingTool extends Item{

	public LinkingTool(){
		super(ESItems.baseItemProperties().stacksTo(1));
		String name = "linking_tool";
		ESItems.queueForRegister(name, this);
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
			tooltip.add(Component.translatable("tt.essentials.linking.info", linked.getX(), linked.getY(), linked.getZ(), dim));
		}else{
			tooltip.add(Component.translatable("tt.essentials.linking.none"));
		}
		tooltip.add(Component.translatable("tt.essentials.linking.desc"));
	}
}
