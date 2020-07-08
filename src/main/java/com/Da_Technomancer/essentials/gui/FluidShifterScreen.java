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

	protected ArrayList<ITextProperties> tooltip = new ArrayList<>();
	private static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/fluid_shifter.png");

	public FluidShifterScreen(FluidShifterContainer container, PlayerInventory playerInventory, ITextComponent text){
		super(container, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();
		if(container.te != null){
			container.te.getFluidManager().initScreen(guiLeft, guiTop, 60, 71, container.fluidIdRef, container.fluidQtyRef);
		}
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		func_230459_a_(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
		if(getSlotUnderMouse() == null){
			renderTooltip(matrix, tooltip, mouseX, mouseY);
		}
		tooltip.clear();
	}

	//MCP note: render screen
	@Override
	protected void func_230450_a_(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		minecraft.getTextureManager().bindTexture(TEXTURE);
		blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize);
		if(container.te != null){
			container.te.getFluidManager().render(matrix, partialTicks, mouseX, mouseY, font, tooltip);
		}
	}
}
