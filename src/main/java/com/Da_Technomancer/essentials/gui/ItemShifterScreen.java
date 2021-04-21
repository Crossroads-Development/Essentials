package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.ItemShifterContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ItemShifterScreen extends ContainerScreen<ItemShifterContainer>{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/item_shifter.png");
	
	public ItemShifterScreen(ItemShifterContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		renderTooltip(matrix, mouseX, mouseY);//MCP note: renderHoveredToolTip
	}

	//MCP note: render screen
	@Override
	protected void renderBg(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		minecraft.getTextureManager().bind(GUI_TEXTURE);
		//drawTexturedModelRectangle
		blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}
