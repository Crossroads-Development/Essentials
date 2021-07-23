package com.Da_Technomancer.essentials.render;

import com.Da_Technomancer.essentials.blocks.WitherCannon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class CannonSkullRenderer extends EntityRenderer<WitherCannon.CannonSkull>{

	private final WitherSkullRenderer witherRenderer;

	public CannonSkullRenderer(EntityRendererProvider.Context context){
		//This class defers to the vanilla Wither Skull renderer wherever possible
		//It exists mainly to get around type differences
		super(context);
		witherRenderer = new WitherSkullRenderer(context);
	}

	@Override
	public ResourceLocation getTextureLocation(WitherCannon.CannonSkull ent){
		return witherRenderer.getTextureLocation(ent);
	}

	@Override
	protected int getBlockLightLevel(WitherCannon.CannonSkull ent, BlockPos pos){
//		Method is not visibly
//		return witherRenderer.getBlockLightLevel(ent, pos);
		return 15;
	}

	@Override
	public void render(WitherCannon.CannonSkull ent, float p_114486_, float p_114487_, PoseStack matrix, MultiBufferSource buffer, int p_114490_){
		witherRenderer.render(ent, p_114486_, p_114487_, matrix, buffer, p_114490_);
	}
}
