package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.FluidShifterContainer;
import com.Da_Technomancer.essentials.tileentities.FluidShifterTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class FluidShifterGuiContainer extends GuiContainer{

	protected FluidShifterTileEntity te;
	private IInventory playerInv;
	protected ArrayList<String> tooltip = new ArrayList<>();
	private CrudeFluidBar fluidBar;
	private static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/fluid_shifter.png");

	protected FluidShifterGuiContainer(FluidShifterContainer container){
		super(container);
		this.te = container.te;
		this.playerInv = container.playerInv;
	}

	@Override
	public void initGui(){
		super.initGui();
		fluidBar = new CrudeFluidBar(this, 0,1, FluidShifterTileEntity.CAPACITY, (width - xSize) / 2, (height - ySize) / 2, 70, 70);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		drawDefaultBackground();
		super.render(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
		if(getSlotUnderMouse() == null){
			drawHoveringText(tooltip, mouseX, mouseY);
		}
		tooltip.clear();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		mc.getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
		if(fluidBar != null){
			fluidBar.drawBack(partialTicks, mouseX, mouseY, fontRenderer);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		if(fluidBar != null){
			fluidBar.drawFore(mouseX, mouseY, fontRenderer);
		}
		fontRenderer.drawString(te.getDisplayName().getFormattedText(), 8, 6, 0x404040);
		fontRenderer.drawString(playerInv.getDisplayName().getFormattedText(), FluidShifterContainer.invStart[0], FluidShifterContainer.invStart[1] - 12, 0x404040);
	}
}
