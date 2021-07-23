package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractCircuit;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.ConstantCircuitContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

@ObjectHolder(Essentials.MODID)
public class ConstantCircuitTileEntity extends CircuitTileEntity implements INamedContainerProvider, INBTReceiver{

	@ObjectHolder("cons_circuit")
	private static TileEntityType<ConstantCircuitTileEntity> TYPE = null;

	public float setting = 0;
	public String settingStr = "0";

	public ConstantCircuitTileEntity(){
		super(TYPE);
	}

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.consCircuit;
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.putFloat("setting", setting);
		nbt.putString("setting_s", settingStr);
		return nbt;
	}

	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt = super.getUpdateTag();
		nbt.putFloat("setting", setting);
		nbt.putString("setting_s", settingStr);
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);
		setting = nbt.getFloat("setting");
		settingStr = nbt.getString("setting_s");
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.cons_circuit");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player){
		return new ConstantCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), worldPosition, settingStr));
	}

	@Override
	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
		setting = nbt.getFloat("value_0");
		settingStr = nbt.getString("text_0");
		setChanged();
		recalculateOutput();
	}
}
