package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.tileentities.CircuitTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class EssentialsEventHandlerClient{

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void renderRedsOutput(RenderWorldLastEvent e){
		PlayerEntity player = Minecraft.getInstance().player;
		//If the player is holding a CircuitWrench (or subclass for addons)
		if(player.getHeldItemMainhand().getItem() instanceof CircuitWrench || player.getHeldItemOffhand().getItem() instanceof CircuitWrench){
			for(TileEntity te : Minecraft.getInstance().world.loadedTileEntityList){
				if(te instanceof CircuitTileEntity){
					float output = ((CircuitTileEntity) te).getOutput();
					Vec3d eyePos = player.getEyePosition(e.getPartialTicks());
					float[] relPos = {te.getPos().getX() + 0.5F - (float) eyePos.x, te.getPos().getY() + 0.5F - (float) eyePos.y, te.getPos().getZ() + 0.5F - (float) eyePos.z};
					if(player.isInRangeToRenderDist(relPos[0] * relPos[0] + relPos[1] * relPos[1] + relPos[2] * relPos[2])){
						GameRenderer.drawNameplate(Minecraft.getInstance().fontRenderer, EssentialsConfig.formatFloat(output, null), relPos[0], relPos[1], relPos[2], 0, player.getYaw(e.getPartialTicks()), player.getPitch(e.getPartialTicks()), false);
					}
				}
			}
		}
	}
}
