package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.packets.ConfigureWrenchOnServer;
import com.Da_Technomancer.essentials.api.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.api.redstone.IWireConnect;
import com.Da_Technomancer.essentials.gui.container.CircuitWrenchContainer;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;

public class CircuitWrenchScreen extends AbstractContainerScreen<CircuitWrenchContainer>{

	private static final ResourceLocation SEARCH_BAR_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/search_bar.png");
	private static final ResourceLocation ROW_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/row.png");
	private static final ResourceLocation MISSING_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/circuit/missing.png");
	private static final int ROWS = 8;
	private static final int COLUMNS = 8;

	private EditBox searchBar;

	/**
	 * A list of the indices of all AbstractTile modes that are currently displayed and match the filter
	 */
	private final ArrayList<Integer> options = new ArrayList<>();

	public CircuitWrenchScreen(CircuitWrenchContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
		imageHeight = 18 * ROWS + 18;
		imageWidth = 18 * COLUMNS;

		for(int i = 0; i < CircuitWrench.MODES.size(); i++){
			options.add(i);
		}
	}

	@Override
	protected void init(){
		super.init();
		searchBar = new EditBox(font, leftPos + 4, topPos + 8, COLUMNS * 18 - 4, 18, Component.translatable("container.search_bar"));
		searchBar.setCanLoseFocus(false);
		searchBar.setTextColor(-1);
		searchBar.setTextColorUneditable(-1);
		searchBar.setBordered(false);
		searchBar.setMaxLength(20);
		searchBar.setResponder(this::filterChanged);
		addWidget(searchBar);
		setInitialFocus(searchBar);
	}

	@Override
	public void resize(Minecraft p_resize_1_, int p_resize_2_, int p_resize_3_){
		String s = searchBar.getValue();
		init(p_resize_1_, p_resize_2_, p_resize_3_);
		searchBar.setValue(s);
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
		if(p_keyPressed_1_ == 256){
			minecraft.player.closeContainer();
		}

		return searchBar.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || searchBar.canConsumeInput() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	/**
	 * @param xPos Target x position
	 * @param yPos Target y position
	 * @return The mode index that is selected, or -1 if none
	 */
	private int getSelectedMode(float xPos, float yPos){
		//Made relative to the start of the modes window
		xPos -= leftPos;
		yPos -= topPos + 18;

		int xInd = (int) Math.floor(xPos / 18F);
		int yInd = (int) Math.floor(yPos / 18F);
		int index = xInd + yInd * COLUMNS;
		if(xInd < 0 || yInd < 0 || xInd >= COLUMNS || yInd >= ROWS || index >= options.size() || index < 0){
			return -1;
		}

		return options.get(index);
	}

	@Override
	public void render(PoseStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);

		//Tooltip
		int index = getSelectedMode(mouseX, mouseY);
		if(index >= 0){
			ArrayList<Component> tt = new ArrayList<>();
			tt.add(Component.translatable(CircuitWrench.MODES.get(index).wireAsBlock().getDescriptionId()));
			IWireConnect block = CircuitWrench.MODES.get(index);
			block.wireAsBlock().appendHoverText(ItemStack.EMPTY, null, tt, TooltipFlag.Default.NORMAL);
			renderComponentTooltip(matrix, tt, mouseX, mouseY);//MCP note: renderTooltip
		}

//		RenderSystem.disableLighting();
//		RenderSystem.disableBlend();
		searchBar.render(matrix, mouseX, mouseY, partialTicks);
	}

	private void filterChanged(String newFilter){
		options.clear();

		for(int i = 0; i < CircuitWrench.MODES.size(); i++){
			IWireConnect tile = CircuitWrench.MODES.get(i);
			String name = I18n.get(tile.wireAsBlock().getDescriptionId()).toLowerCase();
			if(name.contains(newFilter.toLowerCase().trim())){
				options.add(i);
			}
		}
	}

	public static final Style CIRCUIT_WRENCH_STYLE = Style.EMPTY.applyFormat(ChatFormatting.DARK_RED);

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
		minecraft.player.closeContainer();
		minecraft.player.displayClientMessage(Component.translatable("tt.essentials.circuit_wrench_setting").setStyle(CIRCUIT_WRENCH_STYLE).append(Component.translatable(CircuitWrench.MODES.get(index).wireAsBlock().getDescriptionId())), true);//MCP note: setStyle, appendSibling

		return true;
	}

	//MCP note: render screen
	@Override
	protected void renderBg(PoseStack matrix, float partialTicks, int mouseX, int mouseY){
		//Background
		//Search bar
		RenderSystem.setShaderTexture(0, SEARCH_BAR_TEXTURE);
		blit(matrix, leftPos, topPos, 0, 0, imageWidth, 18, imageWidth, 18);

		//Rows
		RenderSystem.setShaderTexture(0, ROW_TEXTURE);
		for(int i = 1; i <= ROWS; i++){
			blit(matrix, leftPos, topPos + i * 18, 0, 0, imageWidth, 18, imageWidth, 18);
		}

		//Foreground
		//Modes
		for(int i = 0; i < options.size(); i++){
			Integer ind = options.get(i);
			ResourceLocation sprite = CircuitWrench.ICONS.get(ind);
			if(sprite == null){
				sprite = MISSING_TEXTURE;
			}
			RenderSystem.setShaderTexture(0, sprite);
			blit(matrix, (i % COLUMNS) * 18 + 1 + leftPos, (i / COLUMNS) * 18 + 19 + topPos, 0, 0, 16, 16, 16, 16);
		}
	}

	//MCP note: draw tooltip/foreground
	@Override
	protected void renderLabels(PoseStack matrix, int p_230451_2_, int p_230451_3_){
		//Don't render text overlays
	}
}
