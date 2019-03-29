package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import com.Da_Technomancer.essentials.tileentities.FluidShifterTileEntity;
import com.Da_Technomancer.essentials.tileentities.ItemShifterTileEntity;
import com.Da_Technomancer.essentials.tileentities.SlottedChestTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.FMLPlayMessages;

import java.util.function.Function;

public class EssentialsGuiHandler implements Function<FMLPlayMessages.OpenContainer, GuiScreen>{

	public static final String SLOTTED_CHEST_GUI = Essentials.MODID + ":slotted_chest";
	public static final String ITEM_SHIFTER_GUI = Essentials.MODID + ":item_shifter";
	public static final String FLUID_SHIFTER_GUI = Essentials.MODID + ":fluid_shifter";

	@Override
	public GuiScreen apply(FMLPlayMessages.OpenContainer openContainer){
		EntityPlayer player = Minecraft.getInstance().player;
		BlockPos pos = openContainer.getAdditionalData().readBlockPos();
		TileEntity te = player.world.getTileEntity(pos);
		switch(openContainer.getId().toString()){
			case SLOTTED_CHEST_GUI:
				return new SlottedChestGuiContainer(player.inventory, (SlottedChestTileEntity) te);
			case ITEM_SHIFTER_GUI:
				return new ItemShifterGuiContainer(player.inventory, (ItemShifterTileEntity) te);
			case FLUID_SHIFTER_GUI:
				return new FluidShifterGuiContainer(new FluidShifterContainer(player.inventory, (FluidShifterTileEntity) te));
		}
		return null;
	}
}
