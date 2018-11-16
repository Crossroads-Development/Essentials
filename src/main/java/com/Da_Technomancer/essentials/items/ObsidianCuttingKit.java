package com.Da_Technomancer.essentials.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ObsidianCuttingKit extends Item{

	protected ObsidianCuttingKit(){
		String name = "obsidian_cutting_kit";
		setTranslationKey(name);
		setRegistryName(name);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		EssentialsItems.toRegister.add(this);
		EssentialsItems.itemAddQue(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced){
		tooltip.add("Insta-breaks obsidian on right click, but uses the tool");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(worldIn.getBlockState(pos).getBlock() == Blocks.OBSIDIAN){
			if(!worldIn.isRemote){
				worldIn.destroyBlock(pos, true);
				if(!playerIn.isCreative()){
					playerIn.getHeldItem(hand).shrink(1);
				}
			}
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
