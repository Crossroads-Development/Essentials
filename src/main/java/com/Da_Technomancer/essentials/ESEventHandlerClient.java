package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractTile;
import com.Da_Technomancer.essentials.gui.CircuitWrenchScreen;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.packets.ConfigureWrenchOnServer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ESEventHandlerClient{

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void renderRedsOutput(RenderWorldLastEvent e){
		LocalPlayer player = Minecraft.getInstance().player;
		//If the player is holding a CircuitWrench (or subclass for addons)
		if(player != null && (player.getMainHandItem().getItem() instanceof CircuitWrench || player.getOffhandItem().getItem() instanceof CircuitWrench)){
			final int RANGE = 64;
			Vec3 eyePos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			PoseStack matrix = e.getMatrixStack();
			matrix.pushPose();
			MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			matrix.translate(-eyePos.x, -eyePos.y, -eyePos.z);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange(player.level, player.blockPosition(), RANGE)){
				if(te instanceof CircuitTileEntity){
					float output = ((CircuitTileEntity) te).getOutput();
					float[] relPos = {te.getBlockPos().getX() + 0.5F, te.getBlockPos().getY() + 0.5F, te.getBlockPos().getZ() + 0.5F};
					if(RANGE * RANGE > Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(relPos[0], relPos[1], relPos[2])){
						renderNameplate(e.getMatrixStack(), buffer, relPos, ESConfig.formatFloat(output, null));
					}
				}
			}
			matrix.popPose();
		}
	}

	private static void renderNameplate(PoseStack matrix, MultiBufferSource.BufferSource buffer, float[] relPos, String nameplate){
		matrix.pushPose();
		matrix.translate(relPos[0], relPos[1], relPos[2]);
		matrix.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
		matrix.scale(-0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = matrix.last().pose();
		Font fontrenderer = Minecraft.getInstance().font;
		float xSt = -fontrenderer.width(nameplate) / 2F;
		fontrenderer.drawInBatch(nameplate, xSt, 0, -1, false, matrix4f, buffer, false, 0, 0xf000f0);
		buffer.endBatch();
		matrix.popPose();
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public void pickBlockCircuitWrench(InputEvent.ClickInputEvent e){
		if(e.isPickBlock() && Minecraft.getInstance().player.getItemInHand(e.getHand()).getItem() == ESItems.circuitWrench){
			//When using pick block on a circuit and holding a circuit wrench, override normal behaviour and set the wrench to that circuit type
			HitResult hit = Minecraft.getInstance().hitResult;
			if(hit.getType() == HitResult.Type.BLOCK){
				BlockPos pos = ((BlockHitResult) hit).getBlockPos();
				Block block = Minecraft.getInstance().level.getBlockState(pos).getBlock();
				if(block instanceof AbstractTile){
					//Because we're on the client side, we need to send a packet to the server updating the wrench

					int index = -1;
					ArrayList<AbstractTile> modes = CircuitWrench.MODES;
					for(int i = 0; i < modes.size(); i++){
						AbstractTile tile = modes.get(i);
						if(tile == block){
							index = i;
							break;
						}
					}
					if(index < 0){
						//Didn't find this circuit
						//Log an error and abort
						Essentials.logger.warn("Attempted to select unregistered circuit: " + block.getRegistryName());
						return;
					}
					e.setCanceled(true);
					EssentialsPackets.channel.sendToServer(new ConfigureWrenchOnServer(index));
					Minecraft.getInstance().player.sendMessage(new TranslatableComponent("tt.essentials.circuit_wrench_setting").setStyle(CircuitWrenchScreen.CIRCUIT_WRENCH_STYLE).append(new TranslatableComponent(CircuitWrench.MODES.get(index).getDescriptionId())), Minecraft.getInstance().player.getUUID());
				}
			}
		}
	}
}
