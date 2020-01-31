package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
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
		if(player != null && (player.getHeldItemMainhand().getItem() instanceof CircuitWrench || player.getHeldItemOffhand().getItem() instanceof CircuitWrench)){
			Vec3d eyePos = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
			MatrixStack matrix = e.getMatrixStack();
			IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
			matrix.translate(-eyePos.x, -eyePos.y, -eyePos.z);
			for(TileEntity te : player.world.loadedTileEntityList){
				if(te instanceof CircuitTileEntity){
					float output = ((CircuitTileEntity) te).getOutput();
					float[] relPos = {te.getPos().getX() + 0.5F, te.getPos().getY() + 0.5F, te.getPos().getZ() + 0.5F};
					if(64 * 64 > Minecraft.getInstance().getRenderManager().getDistanceToCamera(relPos[0], relPos[1], relPos[2])){
						renderNameplate(e.getMatrixStack(), buffer, relPos, ESConfig.formatFloat(output, null));
					}
				}
			}
		}
	}

	private static void renderNameplate(MatrixStack matrix, IRenderTypeBuffer.Impl buffer, float[] relPos, String nameplate){
		matrix.push();
		matrix.translate(relPos[0], relPos[1], relPos[2]);
		matrix.rotate(Minecraft.getInstance().getRenderManager().getCameraOrientation());
		matrix.scale(-0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = matrix.getLast().getPositionMatrix();
		FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
		float xSt = -fontrenderer.getStringWidth(nameplate) / 2F;
		fontrenderer.renderString(nameplate, xSt, 0, -1, false, matrix4f, buffer, false, 0, 0xf000f0);
		buffer.finish();
		matrix.pop();
	}
}
