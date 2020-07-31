package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.container.DelayCircuitContainer;
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
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Predicate;

public class DelayCircuitScreen extends ContainerScreen<DelayCircuitContainer>{

	private static final ResourceLocation SEARCH_BAR_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/search_bar.png");
	private static final ResourceLocation UI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/circuit_filler_back.png");
	private TextFieldWidget delayBar;

	public DelayCircuitScreen(DelayCircuitContainer cont, PlayerInventory playerInventory, ITextComponent text){
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

		delayBar = new TextFieldWidget(font, guiLeft + 18, guiTop + 28, 140, 18, new TranslationTextComponent("container.timer_circuit.period"));

		delayBar.setCanLoseFocus(true);
		delayBar.setTextColor(-1);
		delayBar.setDisabledTextColour(-1);
		delayBar.setEnableBackgroundDrawing(false);
		delayBar.setMaxStringLength(20);
		delayBar.setValidator(textValidator);
		children.add(delayBar);
		delayBar.setText(container.delayStr);
		delayBar.setResponder(this::entryChanged);

		setFocusedDefault(delayBar);
	}

	@Override
	public void resize(Minecraft minecraft, int p_resize_2_, int p_resize_3_){
		String periodS = delayBar.getText();
		init(minecraft, p_resize_2_, p_resize_3_);
		delayBar.setText(periodS);
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
		if(p_keyPressed_1_ == 256){
			minecraft.player.closeScreen();
		}

		return delayBar.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || delayBar.canWrite() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, partialTicks);
		RenderSystem.disableLighting();
		RenderSystem.disableBlend();
		delayBar.render(matrix, mouseX, mouseY, partialTicks);
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

		//Text
		font.func_238422_b_(matrix, new TranslationTextComponent("container.delay_circuit.delay"), guiLeft + 16, guiTop + 12, 0x404040);
	}

	//MCP note: draw tooltip/foreground
	@Override
	protected void func_230451_b_(MatrixStack matrix, int p_230451_2_, int p_230451_3_){
		//Don't render text overlays
	}

	private void entryChanged(String newFilter){
		container.delayStr = delayBar.getText();
		try{
			container.delay = (int) RedstoneUtil.sanitize(Integer.parseInt(container.delayStr));
		}catch(NumberFormatException e){
			container.delay = 2;
		}

		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("value_d", container.delay);
		nbt.putString("config_d", container.delayStr);
		if(container.pos != null){
//			TileEntity te = playerInventory.player.world.getTileEntity(container.pos);
//			if(te instanceof DelayCircuitTileEntity){
//				//We have to set these values on the client tile entity for the output overlay of the circuit wrench to be correct, due to the calculation being done on both sides with TE fields
//				DelayCircuitTileEntity tte = (DelayCircuitTileEntity) te;
//				tte.settingPeriod = container.delay;
//				tte.settingStrPeriod = container.delayStr;
//			}
			EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, container.pos));
		}
	}
}
