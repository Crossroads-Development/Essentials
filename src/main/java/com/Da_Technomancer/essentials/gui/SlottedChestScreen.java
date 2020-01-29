package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class SlottedChestScreen extends ContainerScreen<SlottedChestContainer>{
	
	private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	public SlottedChestScreen(SlottedChestContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
		ySize = 222;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}
	
	/**
	 * Draw the foreground layer for the GuiContainer
	 * (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		font.drawString(title.getFormattedText(), 8, 6, 4210752);
		font.drawString(playerInventory.getDisplayName().getFormattedText(), 8, ySize - 94, 4210752);
		GlStateManager.pushLightingAttributes();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableLighting();
		for(int i = 0; i < 54; i++){
			ItemStack filter = container.filter[i];
			Slot renderSlot = container.inventorySlots.get(i);
			if(!filter.isEmpty() && !renderSlot.getHasStack()){
				itemRenderer.renderItemAndEffectIntoGUI(minecraft.player, filter, renderSlot.xPos, renderSlot.yPos);
	            itemRenderer.renderItemOverlayIntoGUI(font, filter, renderSlot.xPos, renderSlot.yPos, "0");
			}
		}
		GlStateManager.enableLighting();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.popAttributes();
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		RenderSystem.color3f(1, 1, 1);
		minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		//drawTexturedModelRectangle
		blit(i, j, 0, 0, xSize, 125);
		blit(i, j + 125, 0, 126, xSize, 96);
	}
}
