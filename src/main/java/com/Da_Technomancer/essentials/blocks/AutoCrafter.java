package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.AutoCrafterTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class AutoCrafter extends BaseEntityBlock{

	protected AutoCrafter(){
		this("auto_crafter");
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	protected AutoCrafter(String name){
		super(ESBlocks.getMetalProperty());
		setRegistryName(name);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new AutoCrafterTileEntity(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		BlockEntity te;
		if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof AutoCrafterTileEntity){
			AutoCrafterTileEntity acTE = (AutoCrafterTileEntity) te;
			NetworkHooks.openGui((ServerPlayer) playerIn, acTE, pos);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos srcPos, boolean flag){
		if(!world.isClientSide){
			boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
			BlockEntity te = world.getBlockEntity(pos);
			if(te instanceof AutoCrafterTileEntity){
				((AutoCrafterTileEntity) te).redstoneUpdate(powered);
			}
		}
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof AutoCrafterTileEntity) {
				((AutoCrafterTileEntity) te).dropItems();
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.auto_crafter_basic"));
		tooltip.add(new TranslatableComponent("tt.essentials.auto_crafter_book"));

	}
}
