package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.AutoCrafterBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockHitResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class AutoCrafter extends BaseEntityBlock{

	protected AutoCrafter(){
		this("auto_crafter");
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	protected AutoCrafter(String name){
		super(AbstractBlock.Properties.of(Material.METAL).strength(2).sound(SoundType.METAL));
		setRegistryName(name);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(IBlockReader worldIn){
		return new AutoCrafterBlockEntity();
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		BlockEntity te;
		if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof AutoCrafterBlockEntity){
			AutoCrafterBlockEntity acTE = (AutoCrafterBlockEntity) te;
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
			if(te instanceof AutoCrafterBlockEntity){
				((AutoCrafterBlockEntity) te).redstoneUpdate(powered);
			}
		}
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof AutoCrafterBlockEntity) {
				((AutoCrafterBlockEntity) te).dropItems();
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.auto_crafter_basic"));
		tooltip.add(new TranslationTextComponent("tt.essentials.auto_crafter_book"));

	}
}
