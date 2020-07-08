package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractTile;
import com.Da_Technomancer.essentials.gui.container.CircuitWrenchContainer;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.packets.ConfigureWrenchOnServer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
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
		searchBar = new TextFieldWidget(font, guiLeft + 4, guiTop + 8, COLUMNS * 18 - 4, 18, new TranslationTextComponent("container.search_bar"));
		searchBar.setCanLoseFocus(false);
		searchBar.setTextColor(-1);
		searchBar.setDisabledTextColour(-1);
		searchBar.setEnableBackgroundDrawing(false);
		searchBar.setMaxStringLength(20);
		searchBar.setResponder(this::filterChanged);
		children.add(searchBar);
		setFocusedDefault(searchBar);
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

		return searchBar.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || searchBar.canWrite() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	/**
	 * @param xPos Target x position
	 * @param yPos Target y position
	 * @return The mode index that is selected, or -1 if none
	 */
	private int getSelectedMode(float xPos, float yPos){
		//Made relative to the start of the modes window
		xPos -= guiLeft;
		yPos -= guiTop + 18;

		int xInd = (int) Math.floor(xPos / 18F);
		int yInd = (int) Math.floor(yPos / 18F);
		int index = xInd + yInd * COLUMNS;
		if(xInd < 0 || yInd < 0 || xInd >= COLUMNS || yInd >= ROWS || index >= options.size() || index < 0){
			return -1;
		}

		return options.get(index);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);

		//Tooltip
		int index = getSelectedMode(mouseX, mouseY);
		if(index >= 0){
			ArrayList<ITextComponent> tt = new ArrayList<>();
			tt.add(new TranslationTextComponent(CircuitWrench.MODES.get(index).getTranslationKey()));
			AbstractTile block = CircuitWrench.MODES.get(index);
			block.addInformation(ItemStack.EMPTY, null, tt, ITooltipFlag.TooltipFlags.NORMAL);
			renderTooltip(matrix, tt, mouseX, mouseY);
		}

		RenderSystem.disableLighting();
		RenderSystem.disableBlend();
		searchBar.render(matrix, mouseX, mouseY, partialTicks);
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

	private static final Style style = Style.EMPTY.applyFormatting(TextFormatting.DARK_RED);

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
		playerInventory.player.sendMessage(new TranslationTextComponent("tt.essentials.circuit_wrench_setting").func_230530_a_(style).func_230529_a_(new TranslationTextComponent(CircuitWrench.MODES.get(index).getTranslationKey())), playerInventory.player.getUniqueID());//MCP note: setStyle, appendSibling

		return true;
	}

	//MCP note: render screen
	@Override
	protected void func_230450_a_(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		//Search bar
		minecraft.getTextureManager().bindTexture(SEARCH_BAR_TEXTURE);
		blit(matrix, guiLeft, guiTop, 0, 0, xSize, 18, xSize, 18);

		//Rows
		minecraft.getTextureManager().bindTexture(ROW_TEXTURE);
		for(int i = 1; i <= ROWS; i++){
			blit(matrix, guiLeft, guiTop + i * 18, 0, 0, xSize, 18, xSize, 18);
		}

		//Foreground
		//Modes
		for(int i = 0; i < options.size(); i++){
			Integer ind = options.get(i);
			ResourceLocation sprite = CircuitWrench.ICONS.get(ind);
			if(sprite == null){
				sprite = MISSING_TEXTURE;
			}
			minecraft.getTextureManager().bindTexture(sprite);
			blit(matrix, (i % COLUMNS) * 18 + 1 + guiLeft, (i / COLUMNS) * 18 + 19 + guiTop, 0, 0, 16, 16, 16, 16);
		}
	}

	//MCP note: draw tooltip/foreground
	@Override
	protected void func_230451_b_(MatrixStack matrix, int p_230451_2_, int p_230451_3_){
		//Don't render text overlays
	}
}
