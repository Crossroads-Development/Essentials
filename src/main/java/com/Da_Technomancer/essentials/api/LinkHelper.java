package com.Da_Technomancer.essentials.api;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.packets.SendLongToClient;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LinkHelper{

	public static final TagKey<Item> LINKING_TOOLS = ItemTags.create(new ResourceLocation(Essentials.MODID, "linking_tool"));
	public static final String POS_NBT = "c_link";
	public static final String DIM_NBT = "c_link_dim";
	public static final byte LINK_PACKET_ID = 8;//Used to add a link with position encoded into the message
	public static final byte REMOVE_PACKET_ID = 10;//Used to remove a single link with position encoded into the message

	private final Set<BlockPos> linked = new HashSet<>();
	private final ILinkTE te;

	public LinkHelper(ILinkTE te){
		this.te = te;
	}

	/**
	 * Gets the relative positions of all blocks linked to. Allows modification of the underlying links through it.
	 * @return Set of relative positions of all outgoing links.
	 */
	public Set<BlockPos> getLinksRelative(){
		return linked;
	}

	public Iterable<BlockPos> getLinksAbsolute(){
		BlockPos selfPos = te.getTE().getBlockPos();
		return linked.stream().map(relPos -> relPos.offset(selfPos)).collect(Collectors.toList());
	}

	public void readNBT(CompoundTag nbt){
		int i = 0;
		while(nbt.contains("link_" + i)){
			linked.add(BlockPos.of(nbt.getLong("link_" + i)));
			i++;
		}
	}

	/**
	 * Writex this link helper to nbt. Should be used in write() and getUpdatePacket()
	 * @param nbt The nbt compound being written to
	 */
	public void writeNBT(CompoundTag nbt){
		int count = 0;
		for(BlockPos relPos : linked){
			nbt.putLong("link_" + count++, relPos.asLong());
		}
	}

	/**
	 * Creates a link in this TE.
	 * @param endpoint Relative position of endpoint
	 * @param player Player to send chat messages to
	 * @return Whether this linking succeeded
	 */
	public boolean addLink(ILinkTE endpoint, @Nullable Player player){
		if(linked.size() >= te.getMaxLinks()){
			return false;
		}
		BlockPos tePos = te.getTE().getBlockPos();
		BlockPos linkPos = endpoint.getTE().getBlockPos().subtract(tePos);
		linked.add(linkPos);
		BlockUtil.sendClientPacketAround(te.getTE().getLevel(), tePos, new SendLongToClient(LinkHelper.LINK_PACKET_ID, linkPos.asLong(), tePos));
		te.getTE().setChanged();
		return true;
	}

	/**
	 * Removes a link in this TE.
	 * @param endpoint Relative position of endpoint
	 */
	public void removeLink(BlockPos endpoint){
		BlockPos tePos = te.getTE().getBlockPos();
		linked.remove(endpoint);
		BlockUtil.sendClientPacketAround(te.getTE().getLevel(), tePos, new SendLongToClient(LinkHelper.REMOVE_PACKET_ID, endpoint.asLong(), tePos));
		te.getTE().setChanged();
	}

	public void unlinkAllEndpoints(){
		BlockPos selfPos = te.getTE().getBlockPos();
		Level world = te.getTE().getLevel();
		for(BlockPos relPos : linked){
			BlockPos endPos = relPos.offset(selfPos);
			BlockEntity endTe = world.getBlockEntity(endPos);
			if(endTe instanceof ILinkTE){
				((ILinkTE) endTe).removeLinkEnd(selfPos);
			}
		}
	}

	/**
	 * Handles linking related long packets on the client.
	 * @param discriminator The packet discriminator
	 * @param message The packet message
	 * @return True if this used the packet
	 */
	public boolean handleIncomingPacket(byte discriminator, long message){
		if(discriminator == LINK_PACKET_ID){
			linked.add(BlockPos.of(message));
			return true;
		}else if(discriminator == REMOVE_PACKET_ID){
			linked.remove(BlockPos.of(message));
			return true;
		}
		return false;
	}

	/**
	 * Helper method to build a rendering frustrum box with all links
	 * @return A BB for returning to TileEntity::getRenderBoundingBox
	 */
	public AABB frustrum(){
		//Expands the frustrum box to include linked positions
		BlockPos pos = te.getTE().getBlockPos();
		int[] min = new int[3];
		int[] max = new int[3];
		for(BlockPos link : te.getLinks()){
			min[0] = Math.min(min[0], link.getX());
			min[1] = Math.min(min[1], link.getY());
			min[2] = Math.min(min[2], link.getZ());
			max[0] = Math.max(max[0], link.getX());
			max[1] = Math.max(max[1], link.getY());
			max[2] = Math.max(max[2], link.getZ());
		}
		return new AABB(min[0] + pos.getX(), min[1] + pos.getY(), min[2] + pos.getZ(), max[0] + pos.getX() + 1, max[1] + pos.getY() + 1, max[2] + pos.getZ() + 1);
	}

	public static boolean isLinkTool(ItemStack stack){
		return stack.is(LINKING_TOOLS);
	}

	/**
	 * Should be called from the block (server side only) to perform linking
	 * @param linkTE The te that the wrench was used on
	 * @param wrench The held wrench itemstack
	 * @param player The current player
	 * @return The possibly modified wrench
	 */
	public static ItemStack wrench(ILinkTE linkTE, ItemStack wrench, Player player){
		if(player.isCrouching()){
			player.displayClientMessage(Component.translatable("tt.essentials.linking.clear"), true);
			ArrayList<BlockPos> links = new ArrayList<>(linkTE.getLinks());
			Level world = linkTE.getTE().getLevel();
			BlockPos srcPos = linkTE.getTE().getBlockPos();
			for(BlockPos link : links){
				linkTE.removeLinkSource(link);
				BlockEntity endTE = world.getBlockEntity(srcPos.offset(link));
				if(endTE instanceof ILinkTE){
					((ILinkTE) endTE).removeLinkEnd(srcPos);
				}
			}
			clearLinkNBT(wrench);
			return wrench;
		}

		Pair<String, BlockPos> wrenchData = readLinkNBT(wrench);
		if(wrenchData != null && wrenchData.getLeft().equals(getWorldString(player.level))){
			BlockEntity prevTE = player.level.getBlockEntity(wrenchData.getRight());
			if(prevTE instanceof ILinkTE && ((ILinkTE) prevTE).canLink(linkTE) && prevTE != linkTE){
				int range = ((ILinkTE) prevTE).getRange();
				if(wrenchData.getRight().distSqr(linkTE.getTE().getBlockPos()) <= range * range){
					ILinkTE prevLinkTe = (ILinkTE) prevTE;
					//If the link already exists, remove it
					BlockPos relLinkPos = linkTE.getTE().getBlockPos().subtract(prevTE.getBlockPos());
					if(prevLinkTe.getLinks().contains(relLinkPos)){
						prevLinkTe.removeLinkSource(relLinkPos);
						linkTE.removeLinkEnd(prevTE.getBlockPos());
						player.displayClientMessage(Component.translatable("tt.essentials.linking.remove", prevTE.getBlockPos(), linkTE.getTE().getBlockPos()), true);
					}else{
						//Otherwise, create it
						if(prevLinkTe.getLinks().size() < prevLinkTe.getMaxLinks()){
							if(prevLinkTe.createLinkSource(linkTE, player)){
								linkTE.createLinkEnd(prevLinkTe);
								player.displayClientMessage(Component.translatable("tt.essentials.linking.success", prevTE.getBlockPos(), linkTE.getTE().getBlockPos()), true);
							}
						}else{
							player.displayClientMessage(Component.translatable("tt.essentials.linking.full", prevLinkTe.getMaxLinks()), true);
						}
					}
				}else{
					player.displayClientMessage(Component.translatable("tt.essentials.linking.range"), true);
				}
				clearLinkNBT(wrench);
				return wrench;
			}else{
				player.displayClientMessage(Component.translatable("tt.essentials.linking.invalid"), true);
			}
		}else if(linkTE.canBeginLinking()){
			setLinkNBT(wrench, linkTE.getTE().getBlockPos(), getWorldString(linkTE.getTE().getLevel()));
			player.displayClientMessage(Component.translatable("tt.essentials.linking.start"), true);
			return wrench;
		}

		clearLinkNBT(wrench);
		return wrench;
	}

	private static String getWorldString(Level world){
		return world.dimension().location().toString();
	}

	private static void setLinkNBT(ItemStack linkingTool, BlockPos targetPos, String targetWorld){
		CompoundTag itemNBT = linkingTool.getOrCreateTag();
		itemNBT.putLong(POS_NBT, targetPos.asLong());
		itemNBT.putString(DIM_NBT, targetWorld);
	}

	private static void clearLinkNBT(ItemStack linkingTool){
		CompoundTag itemNBT = linkingTool.getTag();
		if(itemNBT != null){
			itemNBT.remove(POS_NBT);
			itemNBT.remove(DIM_NBT);
		}
	}

	@Nullable
	private static Pair<String, BlockPos> readLinkNBT(ItemStack linkingTool){
		CompoundTag itemNBT = linkingTool.getTag();
		if(itemNBT == null){
			return null;
		}
		if(itemNBT.contains(POS_NBT) && itemNBT.contains(DIM_NBT)){
			return Pair.of(itemNBT.getString(DIM_NBT), BlockPos.of(itemNBT.getLong(POS_NBT)));
		}
		return null;
	}

	//Rendering

	public static final ResourceLocation LINK_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/model/link_line.png");
	@OnlyIn(Dist.CLIENT)
	public static final RenderType LINK_TYPE = DummyRenderType.initType();

	/**
	 * @param startPoint Starting position, usually center of TE block position
	 * @param ray Ray to end point, relative to startPoint
	 * @param linkCol The link color. Alpha is ignored
	 * @param partialTicks partial ticks
	 * @param matrix Render matrix, oriented at startpoint
	 * @param buffer Buffer source
	 * @param combinedLight Combined light
	 * @param combinedOverlay Combined overlay
	 */
	@OnlyIn(Dist.CLIENT)
	public static void renderLinkLineToPoint(Vec3 startPoint, Vec3 ray, Color linkCol, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		float alpha = 0.7F;
		int[] col = new int[] {linkCol.getRed(), linkCol.getGreen(), linkCol.getBlue(), (int) (alpha * 255)};
		float uWidth = 1F / 3F;
		Vec3 widthVec = RenderUtil.findRayWidth(startPoint, ray, 0.3F);
		Vec3 normal = ray.cross(widthVec);
		float length = (float) ray.length();

		VertexConsumer builder = buffer.getBuffer(LinkHelper.LINK_TYPE);

		RenderUtil.addVertexBlock(builder, matrix, widthVec.scale(-1), 0, 0, normal, alpha, RenderUtil.BRIGHT_LIGHT);//min-min
		RenderUtil.addVertexBlock(builder, matrix, widthVec, uWidth, 0, normal, alpha, RenderUtil.BRIGHT_LIGHT);//max-min
		RenderUtil.addVertexBlock(builder, matrix, ray.add(widthVec), uWidth, length / 3F, normal, alpha, RenderUtil.BRIGHT_LIGHT);//max-max
		RenderUtil.addVertexBlock(builder, matrix, ray.subtract(widthVec), 0, length / 3F, normal, alpha, RenderUtil.BRIGHT_LIGHT);//min-max

		RenderUtil.addVertexBlock(builder, matrix, widthVec.scale(-1), uWidth, 0, normal, RenderUtil.BRIGHT_LIGHT, col);//min-min
		RenderUtil.addVertexBlock(builder, matrix, widthVec, uWidth * 2, 0, normal, RenderUtil.BRIGHT_LIGHT, col);//max-min
		RenderUtil.addVertexBlock(builder, matrix, ray.add(widthVec), uWidth * 2, length / 3F, normal, RenderUtil.BRIGHT_LIGHT, col);//max-max
		RenderUtil.addVertexBlock(builder, matrix, ray.subtract(widthVec), uWidth, length / 3F, normal, RenderUtil.BRIGHT_LIGHT, col);//min-max
	}

	@OnlyIn(Dist.CLIENT)
	private static class DummyRenderType extends RenderType{

		private DummyRenderType(String p_i225992_1_, VertexFormat p_i225992_2_, VertexFormat.Mode p_i225992_3_, int p_i225992_4_, boolean p_i225992_5_, boolean p_i225992_6_, Runnable p_i225992_7_, Runnable p_i225992_8_){
			super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
		}

		private static RenderType initType(){
			return RenderType.create("link_line", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder().setShaderState(RenderStateShard.BLOCK_SHADER).setTextureState(new TextureStateShard(LINK_TEXTURE, false, false)).setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY).createCompositeState(false));
		}
	}
}
