package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class ConsCircuit extends AbstractCircuit{

	public ConsCircuit(){
		super("cons_circuit");
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return false;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		if(te instanceof ConstantCircuitTileEntity){
			return ((ConstantCircuitTileEntity) te).setting;
		}

		return 0;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		BlockEntity te;
		if(ConfigUtil.isWrench(playerIn.getItemInHand(hand))){
			super.use(state, worldIn, pos, playerIn, hand, hit);
		}else if(playerIn.getItemInHand(hand).getItem() == ESItems.circuitWrench){
			return InteractionResult.PASS;
		}else if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof ConstantCircuitTileEntity){
			NetworkHooks.openScreen((ServerPlayer) playerIn, (MenuProvider) te, buf -> CircuitContainer.encodeData(buf, te.getBlockPos(), ((ConstantCircuitTileEntity) te).settingStr));
		}

		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new ConstantCircuitTileEntity(pos, state);
	}

	@Override
	public boolean usesQuartz(){
		return false;//Considered a 'Basic Circuit'
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.cons_circuit"));
	}
}
