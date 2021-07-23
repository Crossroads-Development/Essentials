package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitBlockEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.TimerCircuitBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockHitResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class TimerCircuit extends AbstractCircuit{

	public TimerCircuit(){
		super("timer_circuit");
	}

	@Override
	public boolean useInput(CircuitBlockEntity.Orient or){
		return false;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitBlockEntity te){
		if(te instanceof TimerCircuitBlockEntity){
			return ((TimerCircuitBlockEntity) te).timerOutput();
		}

		return 0;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		BlockEntity te;
		if(ESConfig.isWrench(playerIn.getItemInHand(hand))){
			super.use(state, worldIn, pos, playerIn, hand, hit);
		}else if(playerIn.getItemInHand(hand).getItem() == ESItems.circuitWrench){
			return InteractionResult.PASS;
		}else if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof TimerCircuitBlockEntity){
			TimerCircuitBlockEntity tte = (TimerCircuitBlockEntity) te;
			NetworkHooks.openGui((ServerPlayer) playerIn, tte, buf -> CircuitContainer.encodeData(buf, te.getBlockPos(), tte.settingStrPeriod, tte.settingStrDuration));
		}

		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(IBlockReader worldIn){
		return new TimerCircuitBlockEntity();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.timer_circuit"));
	}
}
