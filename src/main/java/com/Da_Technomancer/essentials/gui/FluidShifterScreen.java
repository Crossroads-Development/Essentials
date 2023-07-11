package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;

public class FluidShifterScreen extends AbstractContainerScreen<FluidShifterContainer>{

	protected ArrayList<Component> tooltip = new ArrayList<>();
	private static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/fluid_shifter.png");

	public FluidShifterScreen(FluidShifterContainer container, Inventory playerInventory, Component text){
		super(container, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();
		if(menu.te != null){
			menu.te.getFluidManager().initScreen(leftPos, topPos, 60, 71, menu.fluidIdRef, menu.fluidQtyRef);
		}
	}

	@Override
	public void render(GuiGraphics matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		renderTooltip(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
		if(getSlotUnderMouse() == null){
			matrix.renderComponentTooltip(font, tooltip, mouseX, mouseY);//MCP: renderTooltip
		}
		tooltip.clear();
	}

	//MCP note: render screen
	@Override
	protected void renderBg(GuiGraphics matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		matrix.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		if(menu.te != null){
			menu.te.getFluidManager().render(matrix, partialTicks, mouseX, mouseY, font, tooltip);
		}
	}
}
