package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.EssentialsConfig;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.ArrayList;

public final class RedstoneUtil{

	@CapabilityInject(IRedstoneHandler.class)
	public static Capability<IRedstoneHandler> REDSTONE_CAPABILITY = null;

	public static final float MAX_POWER = 1_000_000;

	public static final int DELAY = 2;

	/**
	 * Get the maximum range Essentials redstone signals can travel
	 * @return The maximum range, from the config
	 */
	public static int getMaxRange(){
		return EssentialsConfig.maxRedstoneRange.get();
	}

	/**
	 * Sanitizes a redstone value
	 * @param input The value to sanitize
	 * @return The sanitized value
	 */
	public static float sanitize(float input){
		if(input != input){
			return 0;
		}
		if(input > MAX_POWER){
			return MAX_POWER;
		}
		if(input < 0){
			return 0;
		}
		return input;
	}

	/**
	 * Clamps a redstone signal strength to the vanilla range
	 * @param input The original strength
	 * @return The vanilla redstone strength to emit
	 */
	public static int clampToVanilla(float input){
		input = sanitize(input);
		if(input > 15){
			return 15;
		}
		return Math.round(input);
	}

	/**
	 * Calculates the highest input strength from a list of sources.
	 * Also checks the vanilla redstone power via the world
	 * @param sources The redstone sources to check. May write to the list to remove empty references
	 * @param world The World
	 * @param pos The Position
	 * @param side The side to check signal from
	 * @return The sanitized highest input
	 */
	public static float calcInput(ArrayList<LazyOptional<IRedstoneHandler>> sources, World world, BlockPos pos, Direction side){
		float input = 0;

		for(int i = 0; i < sources.size(); i++){
			LazyOptional<IRedstoneHandler> src = sources.get(i);

			//Clean out empty references so this is quicker in future checks
			if(src == null || !src.isPresent()){
				sources.remove(i);
				i--;
				continue;
			}

			input = Math.max(sanitize(src.orElseThrow(NullPointerException::new).getOutput()), input);
		}

		//Also include vanilla redstone
		input = Math.max(input, world.getRedstonePower(pos.offset(side), side.getOpposite()));

		return input;
	}

	/**
	 * Whether the value effectively changed
	 * Keep in mind that with Java floating point numbers, just because the value changed 8 decimal points down doesn't mean we should consider this changed
	 * @param prevVal The old value
	 * @param newVal The new value
	 * @return Whether the value should be considered "changed"
	 */
	public static boolean didChange(float prevVal, float newVal){
		//If the value changes between zero and nonzero, or if the value changed by more than 0.005%, consider this changed
		return (newVal == 0 ^ prevVal == 0) || Math.abs(newVal - prevVal) / newVal > 0.00_005;
	}


	//Capability registration, not for general use
	public static void registerCap(){
		CapabilityManager.INSTANCE.register(IRedstoneHandler.class, new EmptyStorage(), DefaultRedstoneHandler::new);
	}

	private static class EmptyStorage implements Capability.IStorage<IRedstoneHandler>{

		@Nullable
		@Override
		public INBT writeNBT(Capability<IRedstoneHandler> capability, IRedstoneHandler instance, Direction side){
			return new EndNBT();
		}

		@Override
		public void readNBT(Capability<IRedstoneHandler> capability, IRedstoneHandler instance, Direction side, INBT nbt){

		}
	}
}
