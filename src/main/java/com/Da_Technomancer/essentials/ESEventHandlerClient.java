package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.tileentities.redstone.CircuitTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ESEventHandlerClient{

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void renderRedsOutput(RenderWorldLastEvent e){
		ClientPlayerEntity player = Minecraft.getInstance().player;
		//If the player is holding a CircuitWrench (or subclass for addons)
		if(player != null && (player.getMainHandItem().getItem() instanceof CircuitWrench || player.getOffhandItem().getItem() instanceof CircuitWrench)){
			Vector3d eyePos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			MatrixStack matrix = e.getMatrixStack();
			matrix.pushPose();
			IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
			matrix.translate(-eyePos.x, -eyePos.y, -eyePos.z);
			for(TileEntity te : player.level.blockEntityList){
				if(te instanceof CircuitTileEntity){
					float output = ((CircuitTileEntity) te).getOutput();
					float[] relPos = {te.getBlockPos().getX() + 0.5F, te.getBlockPos().getY() + 0.5F, te.getBlockPos().getZ() + 0.5F};
					if(64 * 64 > Minecraft.getInstance().getEntityRenderDispatcher().distanceToSqr(relPos[0], relPos[1], relPos[2])){
						renderNameplate(e.getMatrixStack(), buffer, relPos, ESConfig.formatFloat(output, null));
					}
				}
			}
			matrix.popPose();
		}
	}

	private static void renderNameplate(MatrixStack matrix, IRenderTypeBuffer.Impl buffer, float[] relPos, String nameplate){
		matrix.pushPose();
		matrix.translate(relPos[0], relPos[1], relPos[2]);
		matrix.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
		matrix.scale(-0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = matrix.last().pose();
		FontRenderer fontrenderer = Minecraft.getInstance().font;
		float xSt = -fontrenderer.width(nameplate) / 2F;
		fontrenderer.drawInBatch(nameplate, xSt, 0, -1, false, matrix4f, buffer, false, 0, 0xf000f0);
		buffer.endBatch();
		matrix.popPose();
	}
}
