package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.redstone.IReadable;
import com.Da_Technomancer.essentials.tileentities.SlottedChestBlockEntity;
import net.minecraft.block.RenderShape;
import net.minecraft.block.BlockState;
import net.minecraft.block.BaseEntityBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.AbstractBlock.Properties;

public class SlottedChest extends BaseEntityBlock implements IReadable{

	protected SlottedChest(){
		super(Properties.of(Material.WOOD).strength(2).sound(SoundType.WOOD));
		String name = "slotted_chest";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public BlockEntity newBlockEntity(IBlockReader world){
		return new SlottedChestBlockEntity();
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof SlottedChestBlockEntity) {
				InventoryHelper.dropContents(worldIn, pos, ((SlottedChestBlockEntity) te).iInv);
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		if(!worldIn.isClientSide){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof SlottedChestBlockEntity){
				ItemStack[] filter = ((SlottedChestBlockEntity) te).lockedInv;
				NetworkHooks.openGui((ServerPlayer) playerIn, (SlottedChestBlockEntity) te, (buf) -> {
					for(ItemStack lock : filter){
						buf.writeItem(lock);
					}
				});
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.slotted_chest.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.slotted_chest.quip").setStyle(ESConfig.TT_QUIP));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_){
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos){
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof SlottedChestBlockEntity){
			float val = ((SlottedChestBlockEntity) te).calcComparator() * 15F;
			val = MathHelper.floor(val * 14.0F) + (val > 0 ? 1 : 0);
			return (int) val;
		}
		return 0;

	}

	@Override
	public float read(Level world, BlockPos pos, BlockState state){
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof SlottedChestBlockEntity){
			return ((SlottedChestBlockEntity) te).calcComparator() * 15F;
		}
		return 0;
	}
}
