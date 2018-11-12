package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.ItemShifterContainer;
import com.Da_Technomancer.essentials.tileentities.ItemShifterTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

public class ItemShifterGuiContainer extends GuiContainer{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/item_shifter.png");
	private final IInventory playerInventory;
	private final ItemShifterTileEntity te;

	protected ItemShifterGuiContainer(IInventory playerInventory, ItemShifterTileEntity te){
		super(new ItemShifterContainer(playerInventory, te));
		this.playerInventory = playerInventory;
		this.te = te;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of
	 * the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		fontRenderer.drawString(te.getDisplayName().getUnformattedText(), 8, 6, 0x404040);
		fontRenderer.drawString(playerInventory.getDisplayName().getUnformattedText(), 8, ySize - 94, 0x404040);
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		mc.getTextureManager().bindTexture(GUI_TEXTURE);
		drawTexturedModalRect((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, ySize);
	}
}
