package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.InterfaceCircuitTileEntity;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ComputerCraftIntegration{

	public static void init(){
		ComputerCraftAPI.registerPeripheralProvider(new CircuitPeripheralProvider());
	}

	private static class CircuitPeripheralProvider implements IPeripheralProvider{

		@Nonnull
		@Override
		public LazyOptional<IPeripheral> getPeripheral(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Direction side){
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() == ESBlocks.interfaceCircuit){
				Direction circuitFace = state.getValue(ESProperties.HORIZ_FACING);
				CircuitTileEntity.Orient orient = CircuitTileEntity.Orient.getOrient(side, circuitFace);
				if(orient == CircuitTileEntity.Orient.FRONT){
					return LazyOptional.of(() -> new CircuitInPeripheral(world, pos));
				}else if(orient == CircuitTileEntity.Orient.BACK){
					return LazyOptional.of(() -> new CircuitOutPeripheral(world, pos));
				}
			}
			return LazyOptional.empty();
		}
	}

	public static class CircuitOutPeripheral implements IPeripheral{

		private final Level level;
		private final BlockPos pos;
		private InterfaceCircuitTileEntity te;

		public CircuitOutPeripheral(Level level, BlockPos pos){
			this.level = level;
			this.pos = pos;
		}

		@Nullable
		private InterfaceCircuitTileEntity getTE(){
			if(te == null){
				BlockEntity newTE = level.getBlockEntity(pos);
				if(newTE instanceof InterfaceCircuitTileEntity){
					te = (InterfaceCircuitTileEntity) newTE;
				}
			}
			return te;
		}

		@Nonnull
		@Override
		public String getType(){
			return "circuit_emitter";
		}

		@Override
		public boolean equals(@Nullable IPeripheral other){
			return other == this || (other instanceof CircuitOutPeripheral cPer && cPer.getTE() == getTE());
		}

		@Nullable
		@Override
		public Object getTarget(){
			return getTE();
		}

		@Override
		public void detach(@Nonnull IComputerAccess computer){
			InterfaceCircuitTileEntity te = getTE();
			if(te != null && te.externalInput != null){
				te.externalInput = null;
				te.setChanged();
				te.handleInputChange(TickPriority.HIGH);
			}
		}

		/**
		 * Sets the output signal of an attached wire splice plate
		 * @param signal New signal strength
		 * @throws LuaException If the block entity doesn't exist and should
		 */
		@SuppressWarnings("unused")
		@LuaFunction
		public final void setCircuitOutput(double signal) throws LuaException{
			InterfaceCircuitTileEntity te = getTE();
			if(te == null){
				throw new LuaException("Circuit peripheral does not exist as a block entity");
			}
			te.externalInput = (float) signal;
			te.setChanged();
			te.handleInputChange(TickPriority.HIGH);
		}
	}

	public static class CircuitInPeripheral implements IPeripheral{

		private final Level level;
		private final BlockPos pos;
		private InterfaceCircuitTileEntity te;

		public CircuitInPeripheral(Level level, BlockPos pos){
			this.level = level;
			this.pos = pos;
		}

		@Nullable
		private InterfaceCircuitTileEntity getTE(){
			if(te == null){
				BlockEntity newTE = level.getBlockEntity(pos);
				if(newTE instanceof InterfaceCircuitTileEntity){
					te = (InterfaceCircuitTileEntity) newTE;
				}
			}
			return te;
		}

		@Nonnull
		@Override
		public String getType(){
			return "circuit_reader";
		}

		@Override
		public boolean equals(@Nullable IPeripheral other){
			return other == this || (other instanceof CircuitInPeripheral cPer && cPer.getTE() == getTE());
		}

		@Nullable
		@Override
		public Object getTarget(){
			return getTE();
		}

		/**
		 * Gets the incoming circuit signal of an attached wire splice plate
		 * @throws LuaException If the block entity doesn't exist and should
		 * @return Current circuit signal strength
		 */
		@SuppressWarnings("unused")
		@LuaFunction
		public final float getCircuitOutput() throws LuaException{
			InterfaceCircuitTileEntity te = getTE();
			if(te == null){
				throw new LuaException("Circuit peripheral does not exist as a block entity");
			}
			return te.getOutput();
		}
	}
}
