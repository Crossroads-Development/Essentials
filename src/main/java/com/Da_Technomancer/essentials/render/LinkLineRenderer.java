package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class LinkLineRenderer<T extends BlockEntity & ILinkTE> implements BlockEntityRenderer<T>{

	public static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/model/link_line.png");
	protected static RenderType LINK_TYPE = DummyRenderType.initType();

	public LinkLineRenderer(BlockEntityRendererProvider.Context context){

	}

	@Override
	public void render(T te, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		//Only render link lines if the player is holding a linking tool
		if(!LinkHelper.isLinkTool(Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND)) && !LinkHelper.isLinkTool(Minecraft.getInstance().player.getItemInHand(InteractionHand.OFF_HAND))){
			return;
		}

		Vec3 tePos = new Vec3(te.getBlockPos().getX() + 0.5, te.getBlockPos().getY() + 0.5, te.getBlockPos().getZ() + 0.5);

		matrix.pushPose();
		matrix.translate(0.5, 0.5, 0.5);
		VertexConsumer builder = buffer.getBuffer(LINK_TYPE);

		Color linkCol = te.getColor();
		float alpha = 0.7F;
		int[] col = new int[] {linkCol.getRed(), linkCol.getGreen(), linkCol.getBlue(), (int) (alpha * 255)};

		float uWidth = 1F / 3F;

		for(BlockPos link : te.getLinks()){
			Vec3 line = Vec3.atLowerCornerOf(link);//A ray pointing from this TE to the link
			Vec3 widthVec = RenderUtil.findRayWidth(tePos, line, 0.3F);
			Vec3 normal = line.cross(widthVec);

			float length = (float) line.length();

			RenderUtil.addVertexBlock(builder, matrix, widthVec.scale(-1), 0, 0, normal, alpha, RenderUtil.BRIGHT_LIGHT);//min-min
			RenderUtil.addVertexBlock(builder, matrix, widthVec, uWidth, 0, normal, alpha, RenderUtil.BRIGHT_LIGHT);//max-min
			RenderUtil.addVertexBlock(builder, matrix, line.add(widthVec), uWidth, length / 3F, normal, alpha, RenderUtil.BRIGHT_LIGHT);//max-max
			RenderUtil.addVertexBlock(builder, matrix, line.subtract(widthVec), 0, length / 3F, normal, alpha, RenderUtil.BRIGHT_LIGHT);//min-max

			RenderUtil.addVertexBlock(builder, matrix, widthVec.scale(-1), uWidth, 0, normal, RenderUtil.BRIGHT_LIGHT, col);//min-min
			RenderUtil.addVertexBlock(builder, matrix, widthVec, uWidth * 2, 0, normal, RenderUtil.BRIGHT_LIGHT, col);//max-min
			RenderUtil.addVertexBlock(builder, matrix, line.add(widthVec), uWidth * 2, length / 3F, normal, RenderUtil.BRIGHT_LIGHT, col);//max-max
			RenderUtil.addVertexBlock(builder, matrix, line.subtract(widthVec), uWidth, length / 3F, normal, RenderUtil.BRIGHT_LIGHT, col);//min-max
		}
		matrix.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen(T te){
		return true;
	}

	private static class DummyRenderType extends RenderType{

		private DummyRenderType(String p_i225992_1_, VertexFormat p_i225992_2_, VertexFormat.Mode p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_){
			super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
		}

		private static RenderType initType(){
			return RenderType.create("link_line", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder().setShaderState(RenderStateShard.BLOCK_SHADER).setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, false)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).createCompositeState(false));
		}
	}
}
