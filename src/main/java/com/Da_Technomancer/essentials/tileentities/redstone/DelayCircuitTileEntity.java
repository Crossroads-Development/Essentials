package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractCircuit;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.DelayCircuitContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.TickPriority;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;

@ObjectHolder(Essentials.MODID)
public class DelayCircuitTileEntity extends CircuitTileEntity implements MenuProvider, INBTReceiver, TickableBlockEntity{

	@ObjectHolder("delay_circuit")
	private static BlockEntityType<DelayCircuitTileEntity> TYPE = null;

	private static final int MIN_DELAY = 1;

	public int settingDelay = 2;
	public String settingStrDelay = "2";

	private long ticksExisted = 0;

	private float currentOutput = 0;//The current output of the circuit
	private final ArrayList<Pair<Float, Long>> queuedOutputs = new ArrayList<>();//A list of each output that will be emitted, paired with the timestamp to start using that output, in chronological order

	public DelayCircuitTileEntity(){
		super(TYPE);
	}

	public float currentOutput(){
		return currentOutput;
	}

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.delayCircuit;
	}

	@Override
	public void handleInputChange(TickPriority priority){
		//Instead of using the vanilla block tick queue, we use our own to allow several different values to be queued in order
		float[] inputs = getInputs(getOwner());
		float input = inputs[1];

		if(queuedOutputs.isEmpty() ? RedstoneUtil.didChange(input, currentOutput) : RedstoneUtil.didChange(input, queuedOutputs.get(queuedOutputs.size() - 1).getLeft())){
			//Add new value to queue
			int delay = RedstoneUtil.DELAY * settingDelay;
			//We pretend ticks existed is an even number for delay, for consistancy with other time based circuits
			queuedOutputs.add(Pair.of(input, delay + ticksExisted - (ticksExisted % RedstoneUtil.DELAY)));
			setChanged();
		}
	}

	@Override
	public void tick(){
		ticksExisted++;

		if(!level.isClientSide && !queuedOutputs.isEmpty()){
			boolean didChange = false;
			long removeTime;
			//We loop this, because vanilla redstone dust de-powering behaviour may lead to multiple entries with the same timestamp, with only the final one being correct
			do{
				Pair<Float, Long> nextInQueue = queuedOutputs.get(0);
				removeTime = nextInQueue.getRight();
				if(removeTime <= ticksExisted){
					queuedOutputs.remove(0);
					currentOutput = nextInQueue.getLeft();
					didChange = true;
				}
			}while(removeTime <= ticksExisted && !queuedOutputs.isEmpty());

			if(didChange){
				//Force circuits to recalculate when output changes
				recalculateOutput();
				setChanged();
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag nbt){
		super.save(nbt);
		nbt.putInt("setting_d", settingDelay);
		nbt.putString("setting_s_d", settingStrDelay);
		nbt.putLong("existed", ticksExisted);
		nbt.putFloat("curr_output", currentOutput);
		for(int i = 0; i < queuedOutputs.size(); i++){
			Pair<Float, Long> outputPair = queuedOutputs.get(i);
			nbt.putFloat(i + "_queue_out", outputPair.getLeft());
			nbt.putLong(i + "_queue_time", outputPair.getRight());
		}
		return nbt;
	}

	@Override
	public void load(BlockState state, CompoundTag nbt){
		super.load(state, nbt);
		settingDelay = nbt.getInt("setting_d");
		settingStrDelay = nbt.getString("setting_s_d");
		ticksExisted = nbt.getLong("existed");
		currentOutput = nbt.getFloat("curr_output");
		int i = 0;
		queuedOutputs.clear();
		while(nbt.contains(i + "_queue_out")){
			queuedOutputs.add(Pair.of(nbt.getFloat(i + "queue_out"), nbt.getLong(i + "queue_time")));
			i++;
			//TODO remove
			if(queuedOutputs.size() == Short.MAX_VALUE){
				//allows recovery of worlds corrupted by a previous bug
				break;
			}
		}
	}

	@Override
	public Component getDisplayName(){
		return new TranslatableComponent("container.delay_circuit");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player){
		return new DelayCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), worldPosition, settingStrDelay));
	}

	@Override
	public void receiveNBT(CompoundTag nbt, @Nullable ServerPlayer sender){
		settingDelay = Math.max(MIN_DELAY, Math.round(nbt.getFloat("value_0")));
		settingStrDelay = nbt.getString("text_0");
		if(!queuedOutputs.isEmpty()){
			//If there is an existing queue, just skip to the final entry
			currentOutput = queuedOutputs.get(queuedOutputs.size() - 1).getLeft();
			queuedOutputs.clear();
		}
		setChanged();
		recalculateOutput();
	}
}
