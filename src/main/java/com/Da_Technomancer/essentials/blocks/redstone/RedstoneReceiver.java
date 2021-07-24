package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import com.Da_Technomancer.essentials.tileentities.redstone.RedstoneReceiverTileEntity;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneReceiver extends BaseEntityBlock implements IWireConnect{

	public RedstoneReceiver(){
		super(BlockBehaviour.Properties.of(Material.STONE).strength(0.5F).sound(SoundType.STONE));
		String name = "redstone_receiver";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
		registerDefaultState(defaultBlockState().setValue(ESProperties.COLOR, DyeColor.WHITE));
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		ItemStack heldItem = playerIn.getItemInHand(hand);
		BlockEntity te = worldIn.getBlockEntity(pos);
		Item dye;
		if(LinkHelper.isLinkTool(heldItem) && te instanceof RedstoneReceiverTileEntity){
			if(!worldIn.isClientSide){
				LinkHelper.wrench((ILinkTE) te, heldItem, playerIn);
			}
			return InteractionResult.SUCCESS;
		}else if((dye = heldItem.getItem()) instanceof DyeItem && te instanceof RedstoneReceiverTileEntity){
			if(!worldIn.isClientSide){
				((RedstoneReceiverTileEntity) te).dye(((DyeItem) dye).getDyeColor());
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.reds_rec.desc"));
		tooltip.add(new TranslatableComponent("tt.essentials.reds_rec.linking"));
		tooltip.add(new TranslatableComponent("tt.essentials.reds_rec.dyes"));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new RedstoneReceiverTileEntity(pos, state);
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side){
		BlockEntity te = blockAccess.getBlockEntity(pos);
		if(te instanceof RedstoneReceiverTileEntity){
			return Math.min(Math.round(((RedstoneReceiverTileEntity) te).getPower()), 15);
		}

		return super.getSignal(blockState, blockAccess, pos, side);
	}

	@Override
	public boolean isSignalSource(BlockState state){
		return true;
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> container){
		container.add(ESProperties.COLOR);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side){
		return true;
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		//Rebuild connections list
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof RedstoneReceiverTileEntity){
			((RedstoneReceiverTileEntity) te).buildDependents();
		}
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		//Rebuild connections list
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof RedstoneReceiverTileEntity){
			((RedstoneReceiverTileEntity) te).buildDependents();
		}
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		if(state.getBlock() != newState.getBlock()){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof RedstoneReceiverTileEntity){
				((RedstoneReceiverTileEntity) te).createLinkEnd(null);
			}
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}
}
