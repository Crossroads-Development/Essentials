package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Fluids;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCandleLily extends ItemLilyPad{

	public ItemCandleLily(){
		super(EssentialsBlocks.candleLilyPad, EssentialsBlocks.itemBlockProp);
		String name = "candle_lilypad";
		setRegistryName(name);
		EssentialsItems.toRegister.add(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TextComponentString("Decorative and light emitting"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand){
		ItemStack itemstack = playerIn.getHeldItem(hand);
		RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);
		if(raytraceresult == null){
			return new ActionResult<>(EnumActionResult.PASS, itemstack);
		}else{
			if(raytraceresult.type == RayTraceResult.Type.BLOCK){
				BlockPos blockpos = raytraceresult.getBlockPos();
				if(!worldIn.isBlockModifiable(playerIn, blockpos) || !playerIn.canPlayerEdit(blockpos.offset(raytraceresult.sideHit), raytraceresult.sideHit, itemstack)){
					return new ActionResult<>(EnumActionResult.FAIL, itemstack);
				}

				BlockPos blockpos1 = blockpos.up();
				IBlockState iblockstate = worldIn.getBlockState(blockpos);
				Material material = iblockstate.getMaterial();
				IFluidState ifluidstate = worldIn.getFluidState(blockpos);
				if((ifluidstate.getFluid() == Fluids.WATER || material == Material.ICE) && worldIn.isAirBlock(blockpos1)){

					// special case for handling block placement with water lilies
					net.minecraftforge.common.util.BlockSnapshot blocksnapshot = net.minecraftforge.common.util.BlockSnapshot.getBlockSnapshot(worldIn, blockpos1);
					worldIn.setBlockState(blockpos1, EssentialsBlocks.candleLilyPad.getDefaultState(), 11);
					if(net.minecraftforge.event.ForgeEventFactory.onBlockPlace(playerIn, blocksnapshot, net.minecraft.util.EnumFacing.UP)){
						blocksnapshot.restore(true, false);
						return new ActionResult<>(EnumActionResult.FAIL, itemstack);
					}

					if(playerIn instanceof EntityPlayerMP){
						CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) playerIn, blockpos1, itemstack);
					}

					if(!playerIn.abilities.isCreativeMode){
						itemstack.shrink(1);
					}

					playerIn.addStat(StatList.ITEM_USED.get(this));
					worldIn.playSound(playerIn, blockpos, SoundEvents.BLOCK_LILY_PAD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
				}
			}

			return new ActionResult<>(EnumActionResult.FAIL, itemstack);
		}
	}
}
