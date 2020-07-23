package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LilyPadItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCandleLily extends LilyPadItem{

	public ItemCandleLily(){
		super(ESBlocks.candleLilyPad, ESBlocks.itemBlockProp);
		String name = "candle_lilypad";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.candle_lilypad.desc"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand){
		ItemStack itemstack = playerIn.getHeldItem(hand);
		RayTraceResult raytraceresult = rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.ANY);
		//Mods sometimes mess with this to return null
		if(!(raytraceresult instanceof BlockRayTraceResult)){
			return new ActionResult<>(ActionResultType.PASS, itemstack);
		}else{
			BlockRayTraceResult result = (BlockRayTraceResult) raytraceresult;
			BlockPos blockpos = result.getPos();
			if(!worldIn.isBlockModifiable(playerIn, blockpos) || !playerIn.canPlayerEdit(blockpos.offset(((BlockRayTraceResult) raytraceresult).getFace()), ((BlockRayTraceResult) raytraceresult).getFace(), itemstack)){
				return new ActionResult<>(ActionResultType.FAIL, itemstack);
			}

			BlockPos blockpos1 = blockpos.up();
			BlockState iblockstate = worldIn.getBlockState(blockpos);
			Material material = iblockstate.getMaterial();
			FluidState ifluidstate = worldIn.getFluidState(blockpos);
			if((ifluidstate.getFluid() == Fluids.WATER || material == Material.ICE) && worldIn.isAirBlock(blockpos1)){

				// special case for handling blocks placement with water lilies
				BlockSnapshot blocksnapshot = net.minecraftforge.common.util.BlockSnapshot.create(worldIn, blockpos1);
				worldIn.setBlockState(blockpos1, ESBlocks.candleLilyPad.getDefaultState(), 11);
				if(ForgeEventFactory.onBlockPlace(playerIn, blocksnapshot, Direction.UP)){
					blocksnapshot.restore(true, false);
					return new ActionResult<>(ActionResultType.FAIL, itemstack);
				}

				if(playerIn instanceof ServerPlayerEntity){
					CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) playerIn, blockpos1, itemstack);
				}

				if(!playerIn.abilities.isCreativeMode){
					itemstack.shrink(1);
				}

				playerIn.addStat(Stats.ITEM_USED.get(this));
				worldIn.playSound(playerIn, blockpos, SoundEvents.BLOCK_LILY_PAD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
			}

			return new ActionResult<>(ActionResultType.FAIL, itemstack);
		}
	}
}
