package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.api.packets.ConfigureWrenchOnServer;
import com.Da_Technomancer.essentials.api.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.api.redstone.IWireConnect;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.blocks.redstone.CircuitTileEntity;
import com.Da_Technomancer.essentials.gui.*;
import com.Da_Technomancer.essentials.gui.container.ESContainers;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.render.CannonSkullRenderer;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class ESEventHandlerClient{

	@EventBusSubscriber(modid = Essentials.MODID, value = Dist.CLIENT)
	public static class ESModEventsClient{

		@SubscribeEvent
		@SuppressWarnings("unused")
		public static void registerScreens(RegisterMenuScreensEvent e){
			e.register(ESContainers.ITEM_SHIFTER_CONTAINER.get(), ItemShifterScreen::new);
			e.register(ESContainers.FLUID_SHIFTER_CONTAINER.get(), FluidShifterScreen::new);
			e.register(ESContainers.SLOTTED_CHEST_CONTAINER.get(), SlottedChestScreen::new);
			e.register(ESContainers.CIRCUIT_WRENCH_CONTAINER.get(), CircuitWrenchScreen::new);
			e.register(ESContainers.CONSTANT_CIRCUIT_CONTAINER.get(), ConstantCircuitScreen::new);
			e.register(ESContainers.TIMER_CIRCUIT_CONTAINER.get(), TimerCircuitScreen::new);
			e.register(ESContainers.DELAY_CIRCUIT_CONTAINER.get(), DelayCircuitScreen::new);
			e.register(ESContainers.PULSE_CIRCUIT_CONTAINER.get(), PulseCircuitScreen::new);
		}

		@SuppressWarnings("unused")
		@SubscribeEvent
		public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e){
			TESRRegistry.init(e);
			e.registerEntityRenderer(WitherCannon.CannonSkull.ENT_TYPE, CannonSkullRenderer::new);
		}
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public static void renderRedsOutput(RenderLevelStageEvent e){
		if(e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES){
			return;
		}
		LocalPlayer player = Minecraft.getInstance().player;
		//If the player is holding a CircuitWrench (or subclass for addons)
		if(player != null && (player.getMainHandItem().getItem() instanceof CircuitWrench || player.getOffhandItem().getItem() instanceof CircuitWrench)){
			final int RANGE = 64;
			Vec3 eyePos = e.getCamera().getPosition();
			PoseStack matrix = e.getPoseStack();
			matrix.pushPose();
			MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			matrix.translate(-eyePos.x, -eyePos.y, -eyePos.z);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange(player.level(), player.blockPosition(), RANGE)){
				IRedstoneHandler handler;
				if(te instanceof CircuitTileEntity circuit && ((handler = circuit.getRedstoneHandler(null)) == null || !handler.shouldMuteOutput())){
					float output = circuit.getOutput();
					float[] relPos = {te.getBlockPos().getX() + 0.5F, te.getBlockPos().getY() + 0.5F, te.getBlockPos().getZ() + 0.5F};
					if(RANGE * RANGE > Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(relPos[0], relPos[1], relPos[2])){
						renderNameplate(e.getPoseStack(), buffer, relPos, ConfigUtil.formatNumberClient(output));
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
//		matrix.mulPose(Axis.YP.rotationDegrees(180));
		matrix.scale(0.025F, -0.025F, -0.025F);
		Matrix4f matrix4f = matrix.last().pose();
		Font fontrenderer = Minecraft.getInstance().font;
		float xSt = -fontrenderer.width(nameplate) / 2F;
		fontrenderer.drawInBatch(nameplate, xSt, 0, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, 0xf000f0);
		buffer.endBatch();
		matrix.popPose();
	}

	@SuppressWarnings("unused")
	@SubscribeEvent
	public static void pickBlockCircuitWrench(InputEvent.InteractionKeyMappingTriggered e){
		if(e.isPickBlock() && Minecraft.getInstance().player.getItemInHand(e.getHand()).getItem() == ESItems.circuitWrench){
			//When using pick block on a circuit and holding a circuit wrench, override normal behaviour and set the wrench to that circuit type
			HitResult hit = Minecraft.getInstance().hitResult;
			if(hit.getType() == HitResult.Type.BLOCK){
				BlockPos pos = ((BlockHitResult) hit).getBlockPos();
				Block block = Minecraft.getInstance().level.getBlockState(pos).getBlock();
				if(block instanceof IWireConnect){
					//Because we're on the client side, we need to send a packet to the server updating the wrench

					int index = -1;
					ArrayList<IWireConnect> modes = CircuitWrench.MODES;
					for(int i = 0; i < modes.size(); i++){
						IWireConnect tile = modes.get(i);
						if(tile == block){
							index = i;
							break;
						}
					}
					if(index < 0){
						//Didn't find this circuit
						/* Several legitimately unregistered IWireConnect instances
						//Log an error and abort
						Essentials.logger.warn("Attempted to select unregistered circuit: " + BuiltInRegistries.BLOCK.getKey(block));
						*/
						return;
					}
					e.setCanceled(true);
					PacketDistributor.sendToServer(new ConfigureWrenchOnServer(index));
					Minecraft.getInstance().player.displayClientMessage(Component.translatable("tt.essentials.circuit_wrench_setting").setStyle(CircuitWrenchScreen.CIRCUIT_WRENCH_STYLE).append(Component.translatable(CircuitWrench.MODES.get(index).wireAsBlock().getDescriptionId())), true);
				}
			}
		}
	}
}
