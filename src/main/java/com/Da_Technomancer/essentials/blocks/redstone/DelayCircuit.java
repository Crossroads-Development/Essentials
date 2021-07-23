package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.DelayCircuitTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class DelayCircuit extends AbstractCircuit{

	public DelayCircuit(){
		super("delay_circuit");
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or == CircuitTileEntity.Orient.BACK;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		if(te instanceof DelayCircuitTileEntity){
			return ((DelayCircuitTileEntity) te).currentOutput();
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
		}else if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof DelayCircuitTileEntity){
			DelayCircuitTileEntity tte = (DelayCircuitTileEntity) te;
			NetworkHooks.openGui((ServerPlayer) playerIn, tte, buf -> CircuitContainer.encodeData(buf, te.getBlockPos(), tte.settingStrDelay));
		}

		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockGetter worldIn){
		return new DelayCircuitTileEntity();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tt.essentials.delay_circuit"));
	}
}
