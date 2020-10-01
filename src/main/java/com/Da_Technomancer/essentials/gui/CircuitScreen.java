package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.SendNBTToServer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Predicate;

public class CircuitScreen<T extends CircuitContainer> extends ContainerScreen<T>{

	protected static final ResourceLocation SEARCH_BAR_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/search_bar.png");
	protected static final ResourceLocation UI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/circuit_filler_back.png");

	protected TextFieldWidget[] inputBars = new TextFieldWidget[container.inputBars()];

	public CircuitScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn){
		super(screenContainer, inv, titleIn);
		xSize = 176;
		ySize = 90;
	}

	private static final Predicate<String> validator = s -> {
		final String whitelist = "0123456789 xX*/+-^piPIeE().";
		for(int i = 0; i < s.length(); i++){
			if(!whitelist.contains(s.substring(i, i + 1))){
				return false;
			}
		}

		return true;
	};

	protected void createTextBar(int id, int x, int y, ITextComponent text){
		inputBars[id] = new TextFieldWidget(font, guiLeft + x, guiTop + y, 144 - 4, 18, text);
		inputBars[id].setCanLoseFocus(true);
		inputBars[id].setTextColor(-1);
		inputBars[id].setDisabledTextColour(-1);
		inputBars[id].setEnableBackgroundDrawing(false);
		inputBars[id].setMaxStringLength(20);
		inputBars[id].setText(container.inputs[id]);
		inputBars[id].setResponder(this::entryChanged);
		inputBars[id].setValidator(validator);
		children.add(inputBars[id]);
//		setFocusedDefault(inputBars[id]);
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height){
		String[] text = new String[inputBars.length];
		for(int i = 0; i < inputBars.length; i++){
			text[i] = inputBars[i].getText();
		}
		init(minecraft, width, height);
		for(int i = 0; i < inputBars.length; i++){
			inputBars[i].setText(text[i]);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if(keyCode == 256){
			minecraft.player.closeScreen();
		}

		for(TextFieldWidget bar : inputBars){
			if(bar.keyPressed(keyCode, scanCode, modifiers) || bar.canWrite()){
				return true;
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		RenderSystem.disableLighting();
		RenderSystem.disableBlend();
		for(TextFieldWidget bar : inputBars){
			bar.render(matrix, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int x, int y){
		//background
		minecraft.getTextureManager().bindTexture(UI_TEXTURE);
		blit(matrix, guiLeft, guiTop, 0, 0, xSize, 90);

		//Text bars
		minecraft.getTextureManager().bindTexture(SEARCH_BAR_TEXTURE);
		for(TextFieldWidget bar : inputBars){
			blit(matrix, bar.x - 2, bar.y - 8, 0, 0, 144, 18, 144, 18);
		}

		//Text labelling input bars
		for(TextFieldWidget inputBar : inputBars){
			font.func_243248_b(matrix, inputBar.getMessage(), inputBar.x - 2, inputBar.y - 16, 0x404040);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int x, int y){
		//Don't render text overlays
	}

	protected void entryChanged(String newFilter){
		CompoundNBT nbt = new CompoundNBT();

		for(int i = 0; i < inputBars.length; i++){
			float output = RedstoneUtil.interpretFormulaString(inputBars[i].getText());
			container.inputs[i] = inputBars[i].getText();
			nbt.putFloat("value_" + i, output);
			nbt.putString("text_" + i, container.inputs[i]);
		}

		if(container.pos != null){
			EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, container.pos));
		}
	}
}
