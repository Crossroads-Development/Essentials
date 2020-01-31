package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LinkLineRenderer<T extends TileEntity & ILinkTE> extends TileEntityRenderer<T>{

	public static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/model/link_line.png");
	protected static RenderType LINK_TYPE = DummyRenderType.initType();

	public LinkLineRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}

	@Override
	public void render(T te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
		//Only render link lines if the player is holding a linking tool
		if(!ILinkTE.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.MAIN_HAND)) && !ILinkTE.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.OFF_HAND))){
			return;
		}

		Vec3d tePos = new Vec3d(te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5);

		matrix.push();
		matrix.translate(0.5, 0.5, 0.5);
		IVertexBuilder builder = buffer.getBuffer(LINK_TYPE);

		for(BlockPos link : te.getLinks()){
			Vec3d line = new Vec3d(link);//A ray pointing from this TE to the link
			Vec3d widthVec = RenderUtil.findRayWidth(tePos, line, 0.3F);
			Vec3d normal = line.crossProduct(widthVec);

			float length = (float) line.length();

			RenderUtil.addVertexBlock(builder, matrix, widthVec.scale(-1), 0, 0, normal, 0.7F, false);//min-min
			RenderUtil.addVertexBlock(builder, matrix, widthVec, 1, 0, normal, 0.7F, false);//max-min
			RenderUtil.addVertexBlock(builder, matrix, line.add(widthVec), 1, length, normal, 0.7F, false);//max-max
			RenderUtil.addVertexBlock(builder, matrix, line.subtract(widthVec), 0, length, normal, 0.7F, false);//min-max
		}
		matrix.pop();
	}

	@Override
	public boolean isGlobalRenderer(T te){
		return true;
	}

	private static class DummyRenderType extends RenderType{

		private DummyRenderType(String p_i225992_1_, VertexFormat p_i225992_2_, int p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_){
			super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
		}

		private static RenderType initType(){
			return RenderType.get("link_line", DefaultVertexFormats.BLOCK, 7, 256, false, true, RenderType.State.builder().texture(new RenderState.TextureState(TEXTURE, false, false)).transparency(RenderState.TRANSLUCENT_TRANSPARENCY).build(false));
		}
	}
}
