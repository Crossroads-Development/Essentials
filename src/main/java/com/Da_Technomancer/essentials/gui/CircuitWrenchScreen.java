package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractTile;
import com.Da_Technomancer.essentials.gui.container.CircuitWrenchContainer;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.packets.ConfigureWrenchOnServer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

public class CircuitWrenchScreen extends ContainerScreen<CircuitWrenchContainer>{

	private static final ResourceLocation SEARCH_BAR_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/search_bar.png");
	private static final ResourceLocation ROW_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/row.png");
	private static final ResourceLocation MISSING_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/circuit/missing.png");
	private static final int ROWS = 8;
	private static final int COLUMNS = 8;

	private TextFieldWidget searchBar;

	/**
	 * A list of the indices of all AbstractTile modes that are currently displayed and match the filter
	 */
	private final ArrayList<Integer> options = new ArrayList<>();

	public CircuitWrenchScreen(CircuitWrenchContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
		ySize = 18 * ROWS + 18;
		xSize = 18 * COLUMNS;

		for(int i = 0; i < CircuitWrench.MODES.size(); i++){
			options.add(i);
		}
	}

	@Override
	protected void init(){
		super.init();
		searchBar = new TextFieldWidget(font, (width - xSize) / 2 + 4, (height - ySize) / 2 + 8, COLUMNS * 18 - 4, 18, I18n.format("container.search_bar"));
		searchBar.setCanLoseFocus(false);
		searchBar.changeFocus(true);
		searchBar.setTextColor(-1);
		searchBar.setDisabledTextColour(-1);
		searchBar.setEnableBackgroundDrawing(false);
		searchBar.setMaxStringLength(20);
		searchBar.func_212954_a(this::filterChanged);
		children.add(searchBar);
		func_212928_a(searchBar);
	}

	@Override
	public void resize(Minecraft p_resize_1_, int p_resize_2_, int p_resize_3_){
		String s = searchBar.getText();
		init(p_resize_1_, p_resize_2_, p_resize_3_);
		searchBar.setText(s);
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
		if(p_keyPressed_1_ == 256){
			minecraft.player.closeScreen();
		}

		return searchBar.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || searchBar.func_212955_f() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	/**
	 * @param xPos Target x position
	 * @param yPos Target y position
	 * @return The mode index that is selected, or -1 if none
	 */
	private int getSelectedMode(float xPos, float yPos){
		//Made relative to the start of the modes window
		xPos -= (width - xSize) / 2;
		yPos -= (height - ySize) / 2 + 18;

		int xInd = (int) (xPos / 18);
		int yInd = (int) (yPos / 18);
		int index = xInd + yInd * COLUMNS;
		if(xInd < 0 || yInd < 0 || xInd >= COLUMNS || yInd >= ROWS || index >= options.size() || index < 0){
			return -1;
		}

		return options.get(index);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		renderBackground();
		super.render(mouseX, mouseY, partialTicks);

		//Tooltip
		int index = getSelectedMode(mouseX, mouseY);
		if(index >= 0){
			renderTooltip(I18n.format(CircuitWrench.MODES.get(index).getTranslationKey()), mouseX, mouseY);
		}

		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		searchBar.render(mouseX, mouseY, partialTicks);
	}

	private void filterChanged(String newFilter){
		options.clear();

		for(int i = 0; i < CircuitWrench.MODES.size(); i++){
			AbstractTile tile = CircuitWrench.MODES.get(i);
			String name = I18n.format(tile.getTranslationKey()).toLowerCase();
			if(name.contains(newFilter.toLowerCase().trim())){
				options.add(i);
			}
		}
	}

	private static final Style style = new Style().setColor(TextFormatting.DARK_RED);

	@Override
	public boolean mouseClicked(double xPos, double yPos, int button){
		if(button != 0){
			return super.mouseClicked(xPos, yPos, button);
		}

		//The index of the AbstractTile mode in the MODES list
		int index = getSelectedMode((int) xPos, (int) yPos);

		if(index < 0){
			//Outside the modes window
			return super.mouseClicked(xPos, yPos, button);
		}

		EssentialsPackets.channel.sendToServer(new ConfigureWrenchOnServer(index));
		minecraft.player.closeScreen();
		playerInventory.player.sendMessage(new TranslationTextComponent("tt.essentials.circuit_wrench_setting").setStyle(style).appendSibling(new TranslationTextComponent(CircuitWrench.MODES.get(index).getTranslationKey())));

		return true;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		//drawTexturedModelRectangle

		//Search bar
		minecraft.getTextureManager().bindTexture(SEARCH_BAR_TEXTURE);
		blit((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, 18, xSize, 18);

		//Rows
		minecraft.getTextureManager().bindTexture(ROW_TEXTURE);
		for(int i = 1; i <= ROWS; i++){
			blit((width - xSize) / 2, (height - ySize) / 2 + i * 18, 0, 0, xSize, 18, xSize, 18);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		//Modes
		for(int i = 0; i < options.size(); i++){
			Integer ind = options.get(i);
			ResourceLocation sprite = CircuitWrench.ICONS.get(ind);
			if(sprite == null){
				sprite = MISSING_TEXTURE;
			}
			minecraft.getTextureManager().bindTexture(sprite);
			blit((i % COLUMNS) * 18 + 1, (i / COLUMNS) * 18 + 19, 0, 0, 16, 16, 16, 16);
		}
	}
}
