package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class PortExtenderTileEntity extends TileEntity{

	/**
	 * The purpose of this variable is to prevent loops of port extenders.
	 */
	private boolean extensionInProgress = false;

	private static boolean supportedCap(Capability<?> cap){
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || cap == CapabilityEnergy.ENERGY;
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side){
		if(!extensionInProgress && supportedCap(cap)){
			TileEntity te = world.getTileEntity(pos.offset(world.getBlockState(pos).getValue(EssentialsProperties.FACING)));
			if(te != null){
				extensionInProgress = true;
				boolean hasCap = te.hasCapability(cap, side);
				extensionInProgress = false;
				return hasCap;
			}
		}	

		return super.hasCapability(cap, side);
	}

	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side){
		if(!extensionInProgress && supportedCap(cap)){
			TileEntity te = world.getTileEntity(pos.offset(world.getBlockState(pos).getValue(EssentialsProperties.FACING)));
			if(te != null){
				extensionInProgress = true;
				T obtainedCap = te.getCapability(cap, side);
				extensionInProgress = false;
				return obtainedCap;
			}
		}	

		return super.getCapability(cap, side);
	}	
}
