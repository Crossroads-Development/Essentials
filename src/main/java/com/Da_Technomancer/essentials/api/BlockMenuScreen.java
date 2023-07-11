package com.Da_Technomancer.essentials.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;

/**
 * A superclass for screens associated with BlockMenuContainers
 * @param <T> The type of BlockMenuContainer
 * @param <U> The type of BlockEntity
 */
public abstract class BlockMenuScreen<T extends BlockMenuContainer<U>, U extends BlockEntity & Container> extends AbstractContainerScreen<T>{

	protected ArrayList<Component> tooltip = new ArrayList<>();

	protected BlockMenuScreen(T container, Inventory playerInventory, Component text){
		super(container, playerInventory, text);
	}

	@Override
	protected void init(){
		super.init();
		inventoryLabelX = menu.getInvStart()[0];
		inventoryLabelY = menu.getInvStart()[1] - 12;
	}

	@Override
	public void render(GuiGraphics matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		renderTooltip(matrix, mouseX, mouseY);
		if(getSlotUnderMouse() == null){
			matrix.renderComponentTooltip(font, tooltip, mouseX, mouseY);
		}
		tooltip.clear();
	}

	@Override
	protected void renderBg(GuiGraphics matrix, float partialTicks, int mouseX, int mouseY){
		//No-op
		//Even though we don't need to implement this method, as this is an abstract class, having this here allows super calls in subclasses.
		//While this currently does nothing, that could change, and allowing super calls in subclasses will make changes seamless
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
		for(GuiEventListener gui : children()){
			if(gui.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)){
				return true;
			}
		}
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean charTyped(char key, int keyCode){
		for(GuiEventListener gui : children()){
			if(gui.charTyped(key, keyCode)){
				return true;
			}
		}

		return super.charTyped(key, keyCode);
	}
}
