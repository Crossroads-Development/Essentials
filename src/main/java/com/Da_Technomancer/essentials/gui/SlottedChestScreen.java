package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.api.BlockMenuScreen;
import com.Da_Technomancer.essentials.blocks.SlottedChestTileEntity;
import com.Da_Technomancer.essentials.gui.container.SlottedChestContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlottedChestScreen extends BlockMenuScreen<SlottedChestContainer, SlottedChestTileEntity>{

	private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

	public SlottedChestScreen(SlottedChestContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
		imageHeight = 222;
	}

	@Override
	public void render(GuiGraphics matrix, int mouseX, int mouseY, float partialTicks){
		super.render(matrix, mouseX, mouseY, partialTicks);
		//We add the ability to render a tooltip for locked empty slots
		ItemStack filterUnderMouse;
		if(menu.getCarried().isEmpty() && hoveredSlot != null && !hoveredSlot.hasItem() && hoveredSlot.container instanceof SlottedChestTileEntity && !(filterUnderMouse = menu.getFilterInSlot(hoveredSlot.getSlotIndex())).isEmpty()){
			//If this is a slot in the slotted chest that is empty, but has a filter, we render a tooltip for the filter item
			matrix.renderTooltip(font, filterUnderMouse, mouseX, mouseY);
		}
	}

	//MCP note: render screen
	@Override
	protected void renderBg(GuiGraphics matrix, float partialTicks, int mouseX, int mouseY){
		super.renderBg(matrix, partialTicks, mouseX, mouseY);
		//Background
		matrix.setColor(1, 1, 1, 1);
		//drawTexturedModelRectangle
		matrix.blit(CHEST_GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, 125);
		matrix.blit(CHEST_GUI_TEXTURE, leftPos, topPos + 125, 0, 126, imageWidth, 96);

		//Foreground
		//Filter items
		for(int i = 0; i < 54; i++){
			ItemStack filter = menu.getFilterInSlot(i);
			Slot renderSlot = menu.slots.get(i);
			if(!filter.isEmpty() && !renderSlot.hasItem()){
				matrix.renderItem(filter, leftPos + renderSlot.x, topPos + renderSlot.y);
				matrix.renderItemDecorations(font, filter, leftPos + renderSlot.x, topPos + renderSlot.y, "0");
			}
		}
	}
}
