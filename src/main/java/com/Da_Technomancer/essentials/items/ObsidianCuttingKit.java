package com.Da_Technomancer.essentials.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ObsidianCuttingKit extends Item{

	public ObsidianCuttingKit(){
		String name = "obsidian_cutting_kit";
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		EssentialsItems.toRegister.add(this);
		EssentialsItems.itemAddQue(this);
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
