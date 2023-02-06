package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.api.packets.ConfigureWrenchOnServer;
import com.Da_Technomancer.essentials.api.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.api.redstone.IWireConnect;
import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.Da_Technomancer.essentials.blocks.redstone.CircuitTileEntity;
import com.Da_Technomancer.essentials.gui.*;
import com.Da_Technomancer.essentials.gui.container.*;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.items.ESItems;
import com.Da_Technomancer.essentials.render.CannonSkullRenderer;
import com.Da_Technomancer.essentials.render.TESRRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.ArrayList;

public class ESEventHandlerClient{

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Essentials.MODID, value = Dist.CLIENT)
	public static class ESModEventsClient{

		@SubscribeEvent
		@SuppressWarnings("unused")
		public static void registerContainers(RegisterEvent e){
			e.register(ForgeRegistries.Keys.MENU_TYPES, helper -> {
				registerCon(ItemShifterContainer::new, ItemShifterScreen::new, "item_shifter", helper);
				registerCon(FluidShifterContainer::new, FluidShifterScreen::new, "fluid_shifter", helper);
				registerCon(SlottedChestContainer::new, SlottedChestScreen::new, "slotted_chest", helper);
				registerCon(CircuitWrenchContainer::new, CircuitWrenchScreen::new, "circuit_wrench", helper);
				registerCon(ConstantCircuitContainer::new, ConstantCircuitScreen::new, "cons_circuit", helper);
				registerCon(TimerCircuitContainer::new, TimerCircuitScreen::new, "timer_circuit", helper);
				registerCon(AutoCrafterContainer::new, AutoCrafterScreen::new, "auto_crafter", helper);
				registerCon(DelayCircuitContainer::new, DelayCircuitScreen::new, "delay_circuit", helper);
				registerCon(PulseCircuitContainer::new, PulseCircuitScreen::new, "pulse_circuit", helper);
			});
		}

		/**
		 * Creates and registers both a container type and a screen factory. Not usable on the physical server due to screen factory.
		 * @param cons Container factory
		 * @param screenFactory The screen factory to be linked to the type
		 * @param id The ID to use
		 * @param helper Registry helper
		 * @param <T> Container subclass
		 */
		private static <T extends AbstractContainerMenu> void registerCon(IContainerFactory<T> cons, MenuScreens.ScreenConstructor<T, AbstractContainerScreen<T>> screenFactory, String id, RegisterEvent.RegisterHelper<MenuType<?>> helper){
			MenuType<T> contType = ESEventHandlerCommon.ESModEventsCommon.registerConType(cons, id, helper);
			MenuScreens.register(contType, screenFactory);
		}

		@SubscribeEvent
		@SuppressWarnings("unused")
		public static void onTextureStitch(TextureStitchEvent.Pre event){
			//Add textures used in TESRs
			//Currently none used
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
			Vec3 eyePos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			PoseStack matrix = e.getPoseStack();
			matrix.pushPose();
			MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			matrix.translate(-eyePos.x, -eyePos.y, -eyePos.z);
			for(BlockEntity te : BlockUtil.getAllLoadedBlockEntitiesRange(player.level, player.blockPosition(), RANGE)){
				if(te instanceof CircuitTileEntity){
					float output = ((CircuitTileEntity) te).getOutput();
					float[] relPos = {te.getBlockPos().getX() + 0.5F, te.getBlockPos().getY() + 0.5F, te.getBlockPos().getZ() + 0.5F};
					if(RANGE * RANGE > Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(relPos[0], relPos[1], relPos[2])){
						renderNameplate(e.getPoseStack(), buffer, relPos, ConfigUtil.formatFloat(output, null));
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
	public static void pickBlockCircuitWrench(InteractionKeyMappingTriggered e){
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
						//Log an error and abort
						Essentials.logger.warn("Attempted to select unregistered circuit: " + ForgeRegistries.BLOCKS.getKey(block));
						return;
					}
					e.setCanceled(true);
					EssentialsPackets.channel.sendToServer(new ConfigureWrenchOnServer(index));
					Minecraft.getInstance().player.displayClientMessage(Component.translatable("tt.essentials.circuit_wrench_setting").setStyle(CircuitWrenchScreen.CIRCUIT_WRENCH_STYLE).append(Component.translatable(CircuitWrench.MODES.get(index).wireAsBlock().getDescriptionId())), true);
				}
			}
		}
	}
}
