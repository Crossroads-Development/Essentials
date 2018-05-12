package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemLilyPad;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCandleLily extends ItemLilyPad{

	public ItemCandleLily(){
		super(EssentialsBlocks.candleLilyPad);
		String name = "candle_lilypad";
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		EssentialsItems.toRegister.add(this);
		EssentialsItems.itemAddQue(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced){
		tooltip.add("Decorative, and light emitting");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand){
		RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);

		if(raytraceresult == null){
			return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(hand));
		}else{
			if(raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK){
				BlockPos blockpos = raytraceresult.getBlockPos();

				if(!worldIn.isBlockModifiable(playerIn, blockpos) || !playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, playerIn.getHeldItem(hand))){
					return new ActionResult<ItemStack>(EnumActionResult.FAIL, playerIn.getHeldItem(hand));
				}

				BlockPos blockpos1 = blockpos.up();
				IBlockState iblockstate = worldIn.getBlockState(blockpos);

				if(iblockstate.getMaterial() == Material.WATER && iblockstate.getValue(BlockLiquid.LEVEL) == 0 && worldIn.isAirBlock(blockpos1)){
					// special case for handling block placement with water
					// lilies
					net.minecraftforge.common.util.BlockSnapshot blocksnapshot = net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot(worldIn, blockpos1);
					worldIn.setBlockState(blockpos1, EssentialsBlocks.candleLilyPad.getDefaultState());
					if(net.minecraftforge.event.ForgeEventFactory.onPlayerBlockPlace(playerIn, blocksnapshot, net.minecraft.util.EnumFacing.UP, hand).isCanceled()){
						blocksnapshot.restore(true, false);
						return new ActionResult<ItemStack>(EnumActionResult.FAIL, playerIn.getHeldItem(hand));
					}

					worldIn.setBlockState(blockpos1, EssentialsBlocks.candleLilyPad.getDefaultState(), 11);

					if(!playerIn.capabilities.isCreativeMode){
						playerIn.getHeldItem(hand).shrink(1);
					}

					playerIn.addStat(StatList.getObjectUseStats(this));
					worldIn.playSound(playerIn, blockpos, SoundEvents.BLOCK_WATERLILY_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
				}
			}

			return new ActionResult<ItemStack>(EnumActionResult.FAIL, playerIn.getHeldItem(hand));
		}
	}

}
