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
		imageHeight = 222;
		//Fixes a vanilla UI bug- the field needs to be recalculated after changing ySize
		inventoryLabelY = imageHeight - 94;//MCP note: player inventory text overlay y position
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		//We add the ability to render a tooltip for locked empty slots
//		renderHoveredTooltip(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
		if(minecraft.player.inventory.getCarried().isEmpty() && hoveredSlot != null){
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
	protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		RenderSystem.color3f(1, 1, 1);
		minecraft.getTextureManager().bind(CHEST_GUI_TEXTURE);
		//drawTexturedModelRectangle
		blit(matrix, leftPos, topPos, 0, 0, imageWidth, 125);
		blit(matrix, leftPos, topPos + 125, 0, 126, imageWidth, 96);

		//Foreground
		RenderSystem.pushLightingAttributes();
		RenderHelper.turnBackOn();
		RenderSystem.disableLighting();
		for(int i = 0; i < 54; i++){
			ItemStack filter = menu.filter[i];
			Slot renderSlot = menu.slots.get(i);
			if(!filter.isEmpty() && !renderSlot.hasItem()){
				itemRenderer.renderAndDecorateItem(minecraft.player, filter, leftPos + renderSlot.x, topPos + renderSlot.y);
				itemRenderer.renderGuiItemDecorations(font, filter, leftPos + renderSlot.x, topPos + renderSlot.y, "0");
			}
		}
		RenderSystem.enableLighting();
		RenderHelper.turnOff();
		RenderSystem.popAttributes();
	}
}
