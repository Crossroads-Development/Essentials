package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;

public class FluidShifterScreen extends ContainerScreen<FluidShifterContainer>{

	protected ArrayList<String> tooltip = new ArrayList<>();
	private static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/fluid_shifter.png");

	public FluidShifterScreen(FluidShifterContainer container, PlayerInventory playerInventory, ITextComponent text){
		super(container, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();
		if(container.te != null){
			container.te.getFluidManager().initScreen((width - xSize) / 2, (height - ySize) / 2, 60, 71);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
		if(getSlotUnderMouse() == null){
			renderTooltip(tooltip, mouseX, mouseY);
		}
		tooltip.clear();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		minecraft.getTextureManager().bindTexture(TEXTURE);
		blit((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
		if(container.te != null){
			container.te.getFluidManager().renderBack(partialTicks, mouseX, mouseY, font);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		if(container.te != null){
			container.te.getFluidManager().renderFore(mouseX, mouseY, font, tooltip);
		}
		font.drawString(title.getFormattedText(), 8, 6, 0x404040);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8, ySize - 94, 0x404040);
	}
}
