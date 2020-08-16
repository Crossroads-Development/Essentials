package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.container.TimerCircuitContainer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.SendNBTToServer;
import com.Da_Technomancer.essentials.tileentities.TimerCircuitTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public class TimerCircuitScreen extends ContainerScreen<TimerCircuitContainer>{

	private static final ResourceLocation SEARCH_BAR_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/search_bar.png");
	private static final ResourceLocation UI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/circuit_filler_back.png");
	private TextFieldWidget periodBar;
	private TextFieldWidget durationBar;

	public TimerCircuitScreen(TimerCircuitContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
		xSize = 176;
		ySize = 90;
	}

	@Override
	protected void init(){
		super.init();
		Predicate<String> textValidator = s -> {
			final String whitelist = "0123456789";
			for(int i = 0; i < s.length(); i++){
				if(!whitelist.contains(s.substring(i, i + 1))){
					return false;
				}
			}

			return true;
		};

		periodBar = new TextFieldWidget(font, guiLeft + 18, guiTop + 28, 140, 18, new TranslationTextComponent("container.timer_circuit.period"));
		durationBar = new TextFieldWidget(font, guiLeft + 18, guiTop + 58, 140, 18, new TranslationTextComponent("container.timer_circuit.duration"));

		periodBar.setCanLoseFocus(true);
		periodBar.setTextColor(-1);
		periodBar.setDisabledTextColour(-1);
		periodBar.setEnableBackgroundDrawing(false);
		periodBar.setMaxStringLength(20);
		periodBar.setValidator(textValidator);
		children.add(periodBar);
		periodBar.setText(container.periodStr);
		periodBar.setResponder(this::entryChanged);

		durationBar.setCanLoseFocus(true);
		durationBar.setTextColor(-1);
		durationBar.setDisabledTextColour(-1);
		durationBar.setEnableBackgroundDrawing(false);
		durationBar.setMaxStringLength(20);
		durationBar.setValidator(textValidator);
		children.add(durationBar);
		durationBar.setText(container.durationStr);
		durationBar.setResponder(this::entryChanged);

		setFocusedDefault(periodBar);
	}

	@Override
	public void resize(Minecraft minecraft, int p_resize_2_, int p_resize_3_){
		String periodS = periodBar.getText();
		String durationS = durationBar.getText();
		init(minecraft, p_resize_2_, p_resize_3_);
		periodBar.setText(periodS);
		durationBar.setText(durationS);
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
		if(p_keyPressed_1_ == 256){
			minecraft.player.closeScreen();
		}

		return periodBar.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || periodBar.canWrite() || durationBar.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || durationBar.canWrite() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		RenderSystem.disableLighting();
		RenderSystem.disableBlend();
		periodBar.render(matrix, mouseX, mouseY, partialTicks);
		durationBar.render(matrix, mouseX, mouseY, partialTicks);
	}

	//MCP note: render screen
	@Override
	protected void func_230450_a_(MatrixStack matrix, float partialTicks, int mouseX, int mouseY){
		//background
		minecraft.getTextureManager().bindTexture(UI_TEXTURE);
		blit(matrix, guiLeft, guiTop, 0, 0, xSize, 90);
		//Text bars
		minecraft.getTextureManager().bindTexture(SEARCH_BAR_TEXTURE);
		blit(matrix, guiLeft + 18 - 2, guiTop + 20, 0, 0, 144, 18, 144, 18);
		blit(matrix, guiLeft + 18 - 2, guiTop + 50, 0, 0, 144, 18, 144, 18);

		//Text
		font.func_243248_b(matrix, new TranslationTextComponent("container.timer_circuit.period"), guiLeft + 16, guiTop + 12, 0x404040);
		font.func_243248_b(matrix, new TranslationTextComponent("container.timer_circuit.duration"), guiLeft + 16, guiTop + 42, 0x404040);
	}

	//MCP note: draw tooltip/foreground
	@Override
	protected void func_230451_b_(MatrixStack matrix, int p_230451_2_, int p_230451_3_){
		//Don't render text overlays
	}

	private void entryChanged(String newFilter){
		container.periodStr = periodBar.getText();
		container.durationStr = durationBar.getText();
		try{
			container.period = (int) RedstoneUtil.sanitize(Integer.parseInt(container.periodStr));
			container.duration = (int) RedstoneUtil.sanitize(Integer.parseInt(container.durationStr));
		}catch(NumberFormatException e){
			container.period = 4;
			container.duration = 2;
		}

		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("value_p", container.period);
		nbt.putString("config_p", container.periodStr);
		nbt.putInt("value_d", container.duration);
		nbt.putString("config_d", container.durationStr);
		if(container.pos != null){
			TileEntity te = playerInventory.player.world.getTileEntity(container.pos);
			if(te instanceof TimerCircuitTileEntity){
				//We have to set these values on the client tile entity for the output overlay of the circuit wrench to be correct, due to the calculation being done on both sides with TE fields
				TimerCircuitTileEntity tte = (TimerCircuitTileEntity) te;
				tte.settingPeriod = container.period;
				tte.settingStrPeriod = container.periodStr;
				tte.settingDuration = container.duration;
				tte.settingStrDuration = container.durationStr;
			}
			EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, container.pos));
		}
	}
}
