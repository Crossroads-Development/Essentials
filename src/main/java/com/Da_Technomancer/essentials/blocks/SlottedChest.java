package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.api.redstone.IReadable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class SlottedChest extends BaseEntityBlock implements IReadable{

	protected SlottedChest(){
		super(Properties.of(Material.WOOD).strength(2).sound(SoundType.WOOD));
		String name = "slotted_chest";
		ESBlocks.toRegister.put(name, this);
		ESBlocks.blockAddQue(name, this);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new SlottedChestTileEntity(pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		if(state.getBlock() != newState.getBlock()){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof SlottedChestTileEntity){
				Containers.dropContents(worldIn, pos, ((SlottedChestTileEntity) te).iInv);
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
			if(te instanceof SlottedChestTileEntity){
				ItemStack[] filter = ((SlottedChestTileEntity) te).lockedInv;
				NetworkHooks.openGui((ServerPlayer) playerIn, (SlottedChestTileEntity) te, (buf) -> {
					for(ItemStack lock : filter){
						buf.writeItem(lock);
					}
				});
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(Component.translatable("tt.essentials.slotted_chest.desc"));
		tooltip.add(Component.translatable("tt.essentials.slotted_chest.quip").setStyle(ConfigUtil.TT_QUIP));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state){
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos){
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof SlottedChestTileEntity){
			float val = ((SlottedChestTileEntity) te).calcComparator() * 15F;
			val = Mth.floor(val * 14.0F) + (val > 0 ? 1 : 0);
			return (int) val;
		}
		return 0;
	}

	@Override
	public float read(Level world, BlockPos pos, BlockState state){
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof SlottedChestTileEntity){
			return ((SlottedChestTileEntity) te).calcComparator() * 15F;
		}
		return 0;
	}
}
