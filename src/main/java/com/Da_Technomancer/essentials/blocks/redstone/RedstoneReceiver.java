package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import com.Da_Technomancer.essentials.tileentities.redstone.RedstoneReceiverTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneReceiver extends ContainerBlock implements IWireConnect{

	public RedstoneReceiver(){
		super(AbstractBlock.Properties.of(Material.STONE).strength(0.5F).sound(SoundType.STONE));
		String name = "redstone_receiver";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
		registerDefaultState(defaultBlockState().setValue(ESProperties.COLOR, DyeColor.WHITE));
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		ItemStack heldItem = playerIn.getItemInHand(hand);
		TileEntity te = worldIn.getBlockEntity(pos);
		Item dye;
		if(LinkHelper.isLinkTool(heldItem) && te instanceof RedstoneReceiverTileEntity){
			if(!worldIn.isClientSide){
				LinkHelper.wrench((ILinkTE) te, heldItem, playerIn);
			}
			return ActionResultType.SUCCESS;
		}else if((dye = heldItem.getItem()) instanceof DyeItem && te instanceof RedstoneReceiverTileEntity){
			if(!worldIn.isClientSide){
				((RedstoneReceiverTileEntity) te).dye(((DyeItem) dye).getDyeColor());
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.reds_rec.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.reds_rec.linking"));
		tooltip.add(new TranslationTextComponent("tt.essentials.reds_rec.dyes"));
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn){
		return new RedstoneReceiverTileEntity();
	}

	@Override
	public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side){
		TileEntity te = blockAccess.getBlockEntity(pos);
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
	public BlockRenderType getRenderShape(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> container){
		container.add(ESProperties.COLOR);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side){
		return true;
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		//Rebuild connections list
		TileEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof RedstoneReceiverTileEntity){
			((RedstoneReceiverTileEntity) te).buildDependents();
		}
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		//Rebuild connections list
		TileEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof RedstoneReceiverTileEntity){
			((RedstoneReceiverTileEntity) te).buildDependents();
		}
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		if(state.getBlock() != newState.getBlock()){
			TileEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof RedstoneReceiverTileEntity){
				((RedstoneReceiverTileEntity) te).createLinkEnd(null);
			}
		}
	}
}
