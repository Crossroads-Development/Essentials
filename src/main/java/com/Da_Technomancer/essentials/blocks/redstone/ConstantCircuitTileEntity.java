package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.packets.INBTReceiver;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESTileEntity;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.ConstantCircuitContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

import static com.Da_Technomancer.essentials.blocks.ESBlocks.consCircuit;

public class ConstantCircuitTileEntity extends CircuitTileEntity implements MenuProvider, INBTReceiver{

	public static final BlockEntityType<ConstantCircuitTileEntity> TYPE = ESTileEntity.createType(ConstantCircuitTileEntity::new, consCircuit);

	public float setting = 0;
	public String settingStr = "0";

	public ConstantCircuitTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.consCircuit;
	}

	@Override
	public void saveAdditional(CompoundTag nbt){
		super.saveAdditional(nbt);
		nbt.putFloat("setting", setting);
		nbt.putString("setting_s", settingStr);
	}

	@Override
	public CompoundTag getUpdateTag(){
		CompoundTag nbt = super.getUpdateTag();
		nbt.putFloat("setting", setting);
		nbt.putString("setting_s", settingStr);
		return nbt;
	}

	@Override
	public void load(CompoundTag nbt){
		super.load(nbt);
		setting = nbt.getFloat("setting");
		settingStr = nbt.getString("setting_s");
	}

	@Override
	public Component getDisplayName(){
		return Component.translatable("container.cons_circuit");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player){
		return new ConstantCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), worldPosition, settingStr));
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		setting = nbt.getFloat("value_0");
		settingStr = nbt.getString("text_0");
		setChanged();
		recalculateOutput();
	}
}
