package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.BlockUtil;
import com.Da_Technomancer.essentials.packets.ILongReceiver;
import com.Da_Technomancer.essentials.packets.SendLongToClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Set;

/**
 * A helper class to be placed on TileEntities to enable basic linking behaviour
 */
public interface ILinkTE extends ILongReceiver{

	ITag<Item> LINKING_TOOLS = ItemTags.makeWrapperTag(new ResourceLocation(Essentials.MODID, "linking_tool").toString());
	String POS_NBT = "c_link";
	String DIM_NBT = "c_link_dim";
	byte LINK_PACKET_ID = 8;
	byte CLEAR_PACKET_ID = 9;

	static boolean isLinkTool(ItemStack stack){
		return LINKING_TOOLS.contains(stack.getItem());
	}

	/**
	 * Helper method to build a rendering frustrum box with all links
	 * @param te The link source
	 * @return A BB for returning to TileEntity::getRenderBoundingBox
	 */
	static AxisAlignedBB frustrum(ILinkTE te){
		//Expands the frustrum box to include linked positions
		BlockPos pos = te.getTE().getPos();
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
		return new AxisAlignedBB(min[0] + pos.getX(), min[1] + pos.getY(), min[2] + pos.getZ(), max[0] + pos.getX() + 1, max[1] + pos.getY() + 1, max[2] + pos.getZ() + 1);
	}

	/**
	 * @return This TE
	 */
	default TileEntity getTE(){
		return (TileEntity) this;
	}

	default int getRange(){
		return 16;
	}

	/**
	 * @return Whether this device can ever be the start of a link
	 */
	boolean canBeginLinking();

	/**
	 *
	 * @param otherTE The ILinkTE to attempt linking to
	 * @return Whether this TE is allowed to link to the otherTE. The source TE controls whether the link is allowed, the target is not checked. Do not check range
	 */
	boolean canLink(ILinkTE otherTE);

	/**
	 * A mutable list of linked relative block positions.
	 * @return The list of links
	 */
	Set<BlockPos> getLinks();

	/**
	 * @return The maximum number of linked devices. The source controls this
	 */
	default int getMaxLinks(){
		return 3;
	}

	/**
	 *
	 * @param endpoint The endpoint that this is being linked to
	 * @param player The calling player, for sending chat messages
	 * @return Whether the operation succeeded
	 */
	default boolean link(ILinkTE endpoint, PlayerEntity player){
		Set<BlockPos> links = getLinks();
		BlockPos linkPos = endpoint.getTE().getPos().subtract(getTE().getPos());
		if(links.contains(linkPos)){
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.taken"), player.getUniqueID());
		}else if(links.size() < getMaxLinks()){
			links.add(linkPos);
			BlockPos tePos = getTE().getPos();
			BlockUtil.sendClientPacketAround(getTE().getWorld(), tePos, new SendLongToClient(LINK_PACKET_ID, linkPos.toLong(), tePos));
			getTE().markDirty();
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.success", getTE().getPos(), endpoint.getTE().getPos()), player.getUniqueID());
			return true;
		}else{
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.full", getMaxLinks()), player.getUniqueID());
		}
		return false;
	}

	/**
	 * Must be called from the block (server side only) to perform linking
	 * @param wrench The held wrench itemstack
	 * @param player The current player   
	 * @return The possibly modified wrench
	 */
	default ItemStack wrench(ItemStack wrench, PlayerEntity player){
		if(player.isCrouching()){
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.clear"), player.getUniqueID());
			clearLinks();
		}else if(wrench.hasTag() && wrench.getTag().contains(POS_NBT) && wrench.getTag().getString(DIM_NBT).equals(player.world.getDimensionKey().getLocation().toString())){//MCP note: get world registry key, get resourcelocation path
			BlockPos prev = BlockPos.fromLong(wrench.getTag().getLong(POS_NBT));

			TileEntity te = player.world.getTileEntity(prev);
			if(te instanceof ILinkTE && ((ILinkTE) te).canLink(this) && te != this){
				if(prev.distanceSq(getTE().getPos()) <= ((ILinkTE) te).getRange() * ((ILinkTE) te).getRange()){
					((ILinkTE) te).link(this, player);
				}else{
					player.sendMessage(new TranslationTextComponent("tt.essentials.linking.range"), player.getUniqueID());
				}
			}else{
				player.sendMessage(new TranslationTextComponent("tt.essentials.linking.invalid"), player.getUniqueID());
			}
		}else if(canBeginLinking()){
			if(!wrench.hasTag()){
				wrench.setTag(new CompoundNBT());
			}

			wrench.getTag().putLong(POS_NBT, getTE().getPos().toLong());
			wrench.getTag().putString(DIM_NBT, getTE().getWorld().getDimensionKey().getLocation().toString());
			player.sendMessage(new TranslationTextComponent("tt.essentials.linking.start"), player.getUniqueID());
			return wrench;
		}

		if(wrench.hasTag()){
			wrench.getTag().remove(POS_NBT);
			wrench.getTag().remove(DIM_NBT);
		}
		return wrench;
	}

	default void clearLinks(){
		getLinks().clear();
		BlockPos tePos = getTE().getPos();
		BlockUtil.sendClientPacketAround(getTE().getWorld(), tePos, new SendLongToClient(CLEAR_PACKET_ID, 0, tePos));
	}
}
