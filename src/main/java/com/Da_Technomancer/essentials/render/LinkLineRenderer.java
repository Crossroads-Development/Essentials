package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
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
import net.minecraft.util.math.vector.Vector3d;

import java.awt.*;

public class LinkLineRenderer<T extends TileEntity & ILinkTE> extends TileEntityRenderer<T>{

	public static final ResourceLocation TEXTURE = new ResourceLocation(Essentials.MODID, "textures/model/link_line.png");
	protected static RenderType LINK_TYPE = DummyRenderType.initType();

	public LinkLineRenderer(TileEntityRendererDispatcher dispatcher){
		super(dispatcher);
	}

	@Override
	public void render(T te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
		//Only render link lines if the player is holding a linking tool
		if(!LinkHelper.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.MAIN_HAND)) && !LinkHelper.isLinkTool(Minecraft.getInstance().player.getHeldItem(Hand.OFF_HAND))){
			return;
		}

		Vector3d tePos = new Vector3d(te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5);

		matrix.push();
		matrix.translate(0.5, 0.5, 0.5);
		IVertexBuilder builder = buffer.getBuffer(LINK_TYPE);

		Color linkCol = te.getColor();
		float alpha = 0.7F;
		int[] col = new int[] {linkCol.getRed(), linkCol.getGreen(), linkCol.getBlue(), (int) (alpha * 255)};

		float uWidth = 1F / 3F;

		for(BlockPos link : te.getLinks()){
			Vector3d line = Vector3d.copy(link);//A ray pointing from this TE to the link
			Vector3d widthVec = RenderUtil.findRayWidth(tePos, line, 0.3F);
			Vector3d normal = line.crossProduct(widthVec);

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
			return RenderType.makeType("link_line", DefaultVertexFormats.BLOCK, 7, 256, false, true, RenderType.State.getBuilder().texture(new RenderState.TextureState(TEXTURE, false, false)).transparency(RenderState.TRANSLUCENT_TRANSPARENCY).build(false));
		}
	}
}
