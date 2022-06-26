package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.api.ILinkTE;
import com.Da_Technomancer.essentials.api.LinkHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class LinkLineRenderer<T extends BlockEntity & ILinkTE> implements BlockEntityRenderer<T>{

	public LinkLineRenderer(BlockEntityRendererProvider.Context context){

	}

	@Override
	public void render(T te, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		//Only render link lines if the player is holding a linking tool
		if(!LinkHelper.isLinkTool(Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND)) && !LinkHelper.isLinkTool(Minecraft.getInstance().player.getItemInHand(InteractionHand.OFF_HAND))){
			return;
		}

		Vec3 tePos = Vec3.atCenterOf(te.getBlockPos());

		matrix.pushPose();
		matrix.translate(0.5, 0.5, 0.5);

		for(BlockPos link : te.getLinks()){
			LinkHelper.renderLinkLineToPoint(tePos, Vec3.atLowerCornerOf(link), te.getColor(), partialTicks, matrix, buffer, combinedLight, combinedOverlay);
		}
		matrix.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen(T te){
		return true;
	}
}
