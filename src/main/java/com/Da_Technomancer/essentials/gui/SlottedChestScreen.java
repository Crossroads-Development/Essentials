package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlottedChestScreen extends AbstractContainerScreen<SlottedChestContainer>{

	private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	public SlottedChestScreen(SlottedChestContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
		imageHeight = 222;
		//Fixes a vanilla UI bug- the field needs to be recalculated after changing ySize
		inventoryLabelY = imageHeight - 94;//MCP note: player inventory text overlay y position
	}

	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		//We add the ability to render a tooltip for locked empty slots
//		renderHoveredTooltip(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
		if(menu.getCarried().isEmpty() && hoveredSlot != null){
			if(hoveredSlot.hasItem()){
				renderTooltip(matrix, hoveredSlot.getItem(), mouseX, mouseY);
			}else if(hoveredSlot.container == menu.inv && hoveredSlot.getSlotIndex() < menu.filter.length && !menu.filter[hoveredSlot.getSlotIndex()].isEmpty()){
				//If this is a slot in the slotted chest that is empty, but has a filter, we render a tooltip for the filter item
				renderTooltip(matrix, menu.filter[hoveredSlot.getSlotIndex()], mouseX, mouseY);
			}
		}
	}

	//MCP note: render screen
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, CHEST_GUI_TEXTURE);
		//drawTexturedModelRectangle
		blit(matrix, leftPos, topPos, 0, 0, imageWidth, 125);
		blit(matrix, leftPos, topPos + 125, 0, 126, imageWidth, 96);

		//Foreground
//		RenderSystem.pushLightingAttributes();
//		Lighting.turnBackOn();
//		RenderSystem.disableLighting();
		for(int i = 0; i < 54; i++){
			ItemStack filter = menu.filter[i];
			Slot renderSlot = menu.slots.get(i);
			if(!filter.isEmpty() && !renderSlot.hasItem()){
				itemRenderer.renderAndDecorateItem(matrix, filter, leftPos + renderSlot.x, topPos + renderSlot.y);
				itemRenderer.renderGuiItemDecorations(matrix, font, filter, leftPos + renderSlot.x, topPos + renderSlot.y, "0");
			}
		}
//		RenderSystem.enableLighting();
//		Lighting.turnOff();
//		RenderSystem.popAttributes();
	}
}
