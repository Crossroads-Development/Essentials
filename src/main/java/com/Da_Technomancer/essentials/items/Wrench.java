package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.EssentialsConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Wrench extends Item{

	protected Wrench(){
		String name = "wrench";
		setTranslationKey(name);
		setRegistryName(name);
		if(EssentialsConfig.getConfigBool(EssentialsConfig.addWrench, false)){
			setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		}
		EssentialsItems.toRegister.add(this);
		EssentialsItems.itemAddQue(this);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player){
		return true;
	}
}
