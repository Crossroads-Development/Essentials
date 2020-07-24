package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;
import com.Da_Technomancer.essentials.tileentities.TimerCircuitTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class TimerCircuit extends AbstractCircuit{

	public TimerCircuit(){
		super("timer_circuit");
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return false;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		if(te instanceof TimerCircuitTileEntity){
			return ((TimerCircuitTileEntity) te).timerOutput();
		}

		return 0;
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		TileEntity te;
		if(ESConfig.isWrench(playerIn.getHeldItem(hand))){
			super.onBlockActivated(state, worldIn, pos, playerIn, hand, hit);
		}else if(playerIn.getHeldItem(hand).getItem() == ESItems.circuitWrench){
			return ActionResultType.PASS;
		}else if(!worldIn.isRemote && (te = worldIn.getTileEntity(pos)) instanceof TimerCircuitTileEntity){
			TimerCircuitTileEntity tte = (TimerCircuitTileEntity) te;
			NetworkHooks.openGui((ServerPlayerEntity) playerIn, tte, buf -> {
				buf.writeInt(tte.settingPeriod);
				buf.writeString(tte.settingStrPeriod);
				buf.writeInt(tte.settingDuration);
				buf.writeString(tte.settingStrDuration);
				buf.writeBlockPos(pos);
			});
		}

		return ActionResultType.SUCCESS;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn){
		return new TimerCircuitTileEntity();
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.timer_circuit"));
	}
}
