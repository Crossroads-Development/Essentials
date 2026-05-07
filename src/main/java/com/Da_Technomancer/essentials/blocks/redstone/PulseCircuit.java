package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.ITickableTileEntity;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

public class PulseCircuit extends AbstractCircuit{

	public final Edge edge;

	public static final MapCodec<PulseCircuit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.INT.fieldOf("edge_ordinal").forGetter((PulseCircuit pulseCircuit) -> pulseCircuit.edge.ordinal())).apply(instance, (Integer edgeOrdinal) -> new PulseCircuit(Edge.values()[edgeOrdinal])));

	public PulseCircuit(Edge edge){
		super("pulse_" + edge.name + "_circuit");
		this.edge = edge;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec(){
		return ESBlocks.PULSE_CIRCUIT_TYPE.value();
	}

	@Override
	public boolean useInput(CircuitTileEntity.Orient or){
		return or == CircuitTileEntity.Orient.BACK;
	}

	@Override
	public float getOutput(float in0, float in1, float in2, CircuitTileEntity te){
		if(te instanceof PulseCircuitTileEntity pulseTE){
			return pulseTE.calculatedOutput();
		}

		return 0;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player playerIn, BlockHitResult hit){
		if(playerIn instanceof ServerPlayer sPlayer && worldIn.getBlockEntity(pos) instanceof PulseCircuitTileEntity tte){
			sPlayer.openMenu(tte, buf -> CircuitContainer.encodeData(buf, tte.getBlockPos(), tte.settingStrDuration));
		}

		return InteractionResult.sidedSuccess(worldIn.isClientSide);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new PulseCircuitTileEntity(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn){
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
