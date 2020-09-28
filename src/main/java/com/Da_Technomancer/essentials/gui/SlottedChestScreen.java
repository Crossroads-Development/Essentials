package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
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
		//Fixes a vanilla UI bug- the field needs to be recalculated after changing ySize
		playerInventoryTitleY = ySize - 94;//MCP note: player inventory text overlay y position
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		//We add the ability to render a tooltip for locked empty slots
//		renderHoveredTooltip(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
		if(minecraft.player.inventory.getItemStack().isEmpty() && hoveredSlot != null){
			if(hoveredSlot.getHasStack()){
				renderTooltip(matrix, hoveredSlot.getStack(), mouseX, mouseY);
			}else if(hoveredSlot.inventory == container.inv && hoveredSlot.getSlotIndex() < container.filter.length && !container.filter[hoveredSlot.getSlotIndex()].isEmpty()){
				//If this is a slot in the slotted chest that is empty, but has a filter, we render a tooltip for the filter item
				renderTooltip(matrix, container.filter[hoveredSlot.getSlotIndex()], mouseX, mouseY);
			}
		}
	}

	//MCP note: render screen
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		RenderSystem.color3f(1, 1, 1);
		minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
		//drawTexturedModelRectangle
		blit(matrix, guiLeft, guiTop, 0, 0, xSize, 125);
		blit(matrix, guiLeft, guiTop + 125, 0, 126, xSize, 96);

		//Foreground
		RenderSystem.pushLightingAttributes();
		RenderHelper.enableStandardItemLighting();
		RenderSystem.disableLighting();
		for(int i = 0; i < 54; i++){
			ItemStack filter = container.filter[i];
			Slot renderSlot = container.inventorySlots.get(i);
			if(!filter.isEmpty() && !renderSlot.getHasStack()){
				itemRenderer.renderItemAndEffectIntoGUI(minecraft.player, filter, guiLeft + renderSlot.xPos, guiTop + renderSlot.yPos);
				itemRenderer.renderItemOverlayIntoGUI(font, filter, guiLeft + renderSlot.xPos, guiTop + renderSlot.yPos, "0");
			}
		}
		RenderSystem.enableLighting();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.popAttributes();
	}
}
