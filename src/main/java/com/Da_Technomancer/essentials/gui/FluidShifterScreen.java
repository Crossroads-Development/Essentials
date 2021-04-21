package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;

import java.util.ArrayList;

public class FluidShifterScreen extends ContainerScreen<FluidShifterContainer>{

	protected ArrayList<ITextComponent> tooltip = new ArrayList<>();
	private static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/fluid_shifter.png");

	public FluidShifterScreen(FluidShifterContainer container, PlayerInventory playerInventory, ITextComponent text){
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
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		renderTooltip(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
		if(getSlotUnderMouse() == null){
			renderComponentTooltip(matrix, tooltip, mouseX, mouseY);//MCP: renderTooltip
		}
		tooltip.clear();
	}

	//MCP note: render screen
	@Override
	protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		minecraft.getTextureManager().bind(TEXTURE);
		blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		if(menu.te != null){
			menu.te.getFluidManager().render(matrix, partialTicks, mouseX, mouseY, font, tooltip);
		}
	}
}
