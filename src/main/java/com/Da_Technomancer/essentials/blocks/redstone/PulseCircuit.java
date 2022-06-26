package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class PulseCircuit extends AbstractCircuit{

	public final Edge edge;

	public PulseCircuit(Edge edge){
		super("pulse_" + edge.name + "_circuit");
		this.edge = edge;
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or == CircuitTileEntity.Orient.BACK;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		if(te instanceof PulseCircuitTileEntity){
			return ((PulseCircuitTileEntity) te).currentOutput(0);
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
		}else if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof PulseCircuitTileEntity){
			PulseCircuitTileEntity tte = (PulseCircuitTileEntity) te;
			NetworkHooks.openGui((ServerPlayer) playerIn, tte, buf -> CircuitContainer.encodeData(buf, te.getBlockPos(), tte.settingStrDuration));
		}

		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new PulseCircuitTileEntity(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.pulse_circuit_" + edge.name));
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type){
		return ITickableTileEntity.createTicker(type, PulseCircuitTileEntity.TYPE);
	}

	public enum Edge{

		RISING(true, false, "rising"),
		FALLING(false, true, "falling"),
		DUAL(true, true, "dual");

		public final String name;
		public final boolean start;
		public final boolean end;

		Edge(boolean start, boolean end, String name){
			this.name = name;
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString(){
			return name;
		}
	}
}
