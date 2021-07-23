package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.PulseCircuit;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.PulseCircuitContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.TickPriority;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

@ObjectHolder(Essentials.MODID)
public class PulseCircuitTileEntity extends CircuitTileEntity implements INamedContainerProvider, INBTReceiver, ITickableTileEntity{

	@ObjectHolder("pulse_circuit")
	private static TileEntityType<PulseCircuitTileEntity> TYPE = null;

	private static final int MIN_DURATION = 1;

	public int settingDuration = 1;
	public String settingStrDuration = "1";

	private long ticksExisted = 0;
	private long pulseStTime = -10;//Negative value means no pulse in progress
	private boolean hadInput = false;

	public PulseCircuitTileEntity(){
		super(TYPE);
	}

	public float currentOutput(int offset){
		long currTime = ticksExisted + offset;
		return pulseStTime <= currTime && settingDuration > (currTime - pulseStTime - 1) / RedstoneUtil.DELAY ? 1 : 0;
	}

	private PulseCircuit.Edge getEdge(){
		Block b = getBlockState().getBlock();
		if(b instanceof PulseCircuit){
			return ((PulseCircuit) b).edge;
		}
		setRemoved();
		return PulseCircuit.Edge.RISING;
	}

	@Override
	public void handleInputChange(TickPriority priority){
		//Instead of using the vanilla block tick queue, we use our own to allow several different values to be queued in order
		float[] inputs = getInputs(getOwner());
		float input = inputs[1];

		boolean activeInput = input > 0;
		if(activeInput != hadInput){
			hadInput = activeInput;
			boolean addPulse;
			if(activeInput){
				addPulse = getEdge().start;
			}else{
				addPulse = getEdge().end;
			}

			if(addPulse){
				pulseStTime = ticksExisted;
			}
			setChanged();
		}
	}

	@Override
	public void tick(){
		ticksExisted++;
		if(!level.isClientSide && RedstoneUtil.didChange(currentOutput(-2), currentOutput(-1))){
			//Force circuits to recalculate when output changes
			recalculateOutput();
			setChanged();
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt){
		super.save(nbt);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
		nbt.putLong("existed", ticksExisted);
		nbt.putLong("st_time", pulseStTime);
		nbt.putBoolean("input", hadInput);
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundNBT nbt){
		super.load(state, nbt);
		settingDuration = nbt.getInt("setting_d");
		settingStrDuration = nbt.getString("setting_s_d");
		ticksExisted = nbt.getLong("existed");
		pulseStTime = nbt.getLong("st_time");
		hadInput = nbt.getBoolean("input");
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.pulse_circuit_" + getEdge().name);
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player){
		return new PulseCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), worldPosition, settingStrDuration));
	}

	@Override
	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
		settingDuration = Math.max(Math.round(nbt.getFloat("value_0")), MIN_DURATION);
		settingStrDuration = nbt.getString("text_0");
		setChanged();
		recalculateOutput();
	}
}
