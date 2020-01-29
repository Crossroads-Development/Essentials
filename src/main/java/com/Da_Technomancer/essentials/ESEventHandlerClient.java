package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.render.LinkLineRenderer;
import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ESEventHandlerClient{

	@SubscribeEvent
	@SuppressWarnings("unused")
	public static void onTextureStitch(TextureStitchEvent.Pre event){
		//Add textures used in TESRs
		if(event.getMap().getBasePath().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE)){
			event.addSprite(LinkLineRenderer.TEXTURE);
		}
	}

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void renderRedsOutput(RenderWorldLastEvent e){
		PlayerEntity player = Minecraft.getInstance().player;
		//If the player is holding a CircuitWrench (or subclass for addons)
		if(player != null && (player.getHeldItemMainhand().getItem() instanceof CircuitWrench || player.getHeldItemOffhand().getItem() instanceof CircuitWrench)){
			for(TileEntity te : player.world.loadedTileEntityList){
				if(te instanceof CircuitTileEntity){
					float output = ((CircuitTileEntity) te).getOutput();
					Vec3d eyePos = player.getEyePosition(e.getPartialTicks());
					float[] relPos = {te.getPos().getX() + 0.5F - (float) eyePos.x, te.getPos().getY() + 0.5F - (float) eyePos.y, te.getPos().getZ() + 0.5F - (float) eyePos.z};
					if(player.isInRangeToRenderDist(relPos[0] * relPos[0] + relPos[1] * relPos[1] + relPos[2] * relPos[2])){
						renderNameplate(e.getMatrixStack(), relPos, ESConfig.formatFloat(output, null));
//						GameRenderer.drawNameplate(Minecraft.getInstance().fontRenderer, ESConfig.formatFloat(output, null), relPos[0], relPos[1], relPos[2], 0, player.getYaw(e.getPartialTicks()), player.getPitch(e.getPartialTicks()), false);
					}
				}
			}
		}
	}

	//TODO test this
	private static void renderNameplate(MatrixStack matrix, float[] relPos, String nameplate){
		matrix.push();
		RenderSystem.pushLightingAttributes();
		matrix.translate(relPos[0], relPos[1], relPos[2]);
		matrix.rotate(Minecraft.getInstance().getRenderManager().getCameraOrientation());
		matrix.scale(-0.025F, -0.025F, 0.025F);
		Matrix4f matrix4f = matrix.getLast().getPositionMatrix();
		FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
		float f2 = -fontrenderer.getStringWidth(nameplate) / 2F;
		fontrenderer.renderString(nameplate, f2, 0, -1, false, matrix4f, Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(), false, 0, 0xf000f0);
		RenderSystem.popAttributes();
		matrix.pop();
	}
}
