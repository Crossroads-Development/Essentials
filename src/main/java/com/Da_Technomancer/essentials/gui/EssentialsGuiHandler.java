package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.Da_Technomancer.essentials.tileentities.SlottedChestTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class EssentialsGuiHandler implements IGuiHandler{

	public static final int SLOTTED_CHEST_GUI = 0;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		switch(ID){
			case SLOTTED_CHEST_GUI:
				return new SlottedChestContainer(player.inventory, ((SlottedChestTileEntity) world.getTileEntity(new BlockPos(x, y, z))));
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		switch(ID){
			case SLOTTED_CHEST_GUI:
				return new SlottedChestGuiContainer(player.inventory, ((SlottedChestTileEntity) world.getTileEntity(new BlockPos(x, y, z))));
		}

		return null;
	}

}
