package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.packets.SendLongToClient;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LinkHelper{

	public static final TagKey<Item> LINKING_TOOLS = ItemTags.create(new ResourceLocation(Essentials.MODID, "linking_tool"));
	public static final String POS_NBT = "c_link";
	public static final String DIM_NBT = "c_link_dim";
	public static final byte LINK_PACKET_ID = 8;//Used to add a link with position encoded into the message
	@Deprecated
	public static final byte CLEAR_PACKET_ID = 9;//Used to remove all links
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
			player.sendMessage(new TranslatableComponent("tt.essentials.linking.clear"), player.getUUID());
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
						player.sendMessage(new TranslatableComponent("tt.essentials.linking.remove", prevTE.getBlockPos(), linkTE.getTE().getBlockPos()), player.getUUID());
					}else{
						//Otherwise, create it
						if(prevLinkTe.getLinks().size() < prevLinkTe.getMaxLinks()){
							if(prevLinkTe.createLinkSource(linkTE, player)){
								linkTE.createLinkEnd(prevLinkTe);
								player.sendMessage(new TranslatableComponent("tt.essentials.linking.success", prevTE.getBlockPos(), linkTE.getTE().getBlockPos()), player.getUUID());
							}
						}else{
							player.sendMessage(new TranslatableComponent("tt.essentials.linking.full", prevLinkTe.getMaxLinks()), player.getUUID());
						}
					}
				}else{
					player.sendMessage(new TranslatableComponent("tt.essentials.linking.range"), player.getUUID());
				}
				clearLinkNBT(wrench);
				return wrench;
			}else{
				player.sendMessage(new TranslatableComponent("tt.essentials.linking.invalid"), player.getUUID());
			}
		}else if(linkTE.canBeginLinking()){
			setLinkNBT(wrench, linkTE.getTE().getBlockPos(), getWorldString(linkTE.getTE().getLevel()));
			player.sendMessage(new TranslatableComponent("tt.essentials.linking.start"), player.getUUID());
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
}
