package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.redstone.CircuitTileEntity;
import com.Da_Technomancer.essentials.blocks.redstone.InterfaceCircuitTileEntity;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.core.Direction;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ComputerCraftIntegration{

	protected static void registerComputerCapabilities(RegisterCapabilitiesEvent e){
		e.registerBlock(PeripheralCapability.get(), CIRCUIT_PERIPHERAL_PROVIDER, ESBlocks.interfaceCircuit);
	}

	private static final IBlockCapabilityProvider<IPeripheral, Direction> CIRCUIT_PERIPHERAL_PROVIDER = (level, pos, state, te, side) -> {
		if(te instanceof InterfaceCircuitTileEntity interfaceTE && state.getBlock() == ESBlocks.interfaceCircuit){
			Direction circuitFace = state.getValue(ESProperties.HORIZ_FACING);
			CircuitTileEntity.Orient orient = CircuitTileEntity.Orient.getOrient(side, circuitFace);
			if(orient == CircuitTileEntity.Orient.FRONT){
				return new CircuitInPeripheral(interfaceTE);
			}else if(orient == CircuitTileEntity.Orient.BACK){
				return new CircuitOutPeripheral(interfaceTE);
			}
		}
		return null;
	};

	public static class CircuitOutPeripheral implements IPeripheral{

		private final InterfaceCircuitTileEntity te;

		public CircuitOutPeripheral(InterfaceCircuitTileEntity te){
			this.te = te;
		}

		@Nonnull
		@Override
		public String getType(){
			return "circuit_emitter";
		}

		@Override
		public boolean equals(@Nullable IPeripheral o){
			if(o == null) return false;
			CircuitOutPeripheral that = (CircuitOutPeripheral) o;
			return Objects.equals(te, that.te);
		}

		@Nullable
		@Override
		public Object getTarget(){
			return te;
		}

		/**
		 * Sets the output signal of an attached wire splice plate
		 * @param signal New signal strength
		 * @throws LuaException If the block entity doesn't exist and should
		 */
		@SuppressWarnings("unused")
		@LuaFunction(mainThread = true)
		public final void setCircuitOutput(double signal) throws LuaException{
			if(te == null){
				throw new LuaException("Circuit peripheral does not exist as a block entity");
			}
			te.externalInput = (float) signal;
			te.setChanged();
			te.handleInputChange(TickPriority.HIGH);
		}

		/**
		 * Stops computer control of the output signal of an attached wire splice plate
		 * @throws LuaException If the block entity doesn't exist and should
		 */
		@SuppressWarnings("unused")
		@LuaFunction(mainThread = true)
		public final void resetCircuitOutput() throws LuaException{
			if(te == null){
				throw new LuaException("Circuit peripheral does not exist as a block entity");
			}
			te.externalInput = null;
			te.setChanged();
			te.handleInputChange(TickPriority.HIGH);
		}
	}

	public static class CircuitInPeripheral implements IPeripheral{

		private final InterfaceCircuitTileEntity te;

		public CircuitInPeripheral(InterfaceCircuitTileEntity te){
			this.te = te;
		}

		@Nonnull
		@Override
		public String getType(){
			return "circuit_reader";
		}

		@Override
		public boolean equals(@Nullable IPeripheral o){
			if(o == null) return false;
			CircuitInPeripheral that = (CircuitInPeripheral) o;
			return Objects.equals(te, that.te);
		}

		@Nullable
		@Override
		public Object getTarget(){
			return te;
		}

		/**
		 * Gets the incoming circuit signal of an attached wire splice plate
		 * @throws LuaException If the block entity doesn't exist and should
		 * @return Current circuit signal strength
		 */
		@SuppressWarnings("unused")
		@LuaFunction(mainThread = true)
		public final float getCircuitOutput() throws LuaException{
			if(te == null){
				throw new LuaException("Circuit peripheral does not exist as a block entity");
			}
			return te.getOutput();
		}
	}
}
