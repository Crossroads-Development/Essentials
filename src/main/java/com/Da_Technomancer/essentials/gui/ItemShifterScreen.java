package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.ItemShifterContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemShifterScreen extends AbstractContainerScreen<ItemShifterContainer>{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/item_shifter.png");

	public ItemShifterScreen(ItemShifterContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
	}

	@Override
	public void render(GuiGraphics matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		renderTooltip(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
	}

	//MCP note: render screen
	@Override
	protected void renderBg(GuiGraphics matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		matrix.blit(GUI_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
