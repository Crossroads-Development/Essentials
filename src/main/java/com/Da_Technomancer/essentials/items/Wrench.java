package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.EssentialsConfig;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class Wrench extends Item{

	protected Wrench(){
		super(new Item.Properties().maxStackSize(1).group(EssentialsConfig.addWrench.get() ? EssentialsItems.TAB_ESSENTIALS : null));
		String name = "wrench";
		setRegistryName(name);
		EssentialsItems.toRegister.add(this);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, EntityPlayer player){
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TextComponentString("Rotates & configures blocks from " + Essentials.MODNAME));
	}
}
