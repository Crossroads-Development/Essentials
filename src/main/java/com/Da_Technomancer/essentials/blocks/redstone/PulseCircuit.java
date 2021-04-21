package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.PulseCircuitTileEntity;
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
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		TileEntity te;
		if(ESConfig.isWrench(playerIn.getItemInHand(hand))){
			super.use(state, worldIn, pos, playerIn, hand, hit);
		}else if(playerIn.getItemInHand(hand).getItem() == ESItems.circuitWrench){
			return ActionResultType.PASS;
		}else if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof PulseCircuitTileEntity){
			PulseCircuitTileEntity tte = (PulseCircuitTileEntity) te;
			NetworkHooks.openGui((ServerPlayerEntity) playerIn, tte, buf -> CircuitContainer.encodeData(buf, te.getBlockPos(), tte.settingStrDuration));
		}

		return ActionResultType.SUCCESS;
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn){
		return new PulseCircuitTileEntity();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.pulse_circuit_" + edge.name));
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
