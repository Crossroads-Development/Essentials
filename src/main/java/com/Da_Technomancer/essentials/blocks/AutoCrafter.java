package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.AutoCrafterTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class AutoCrafter extends ContainerBlock{

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
	public TileEntity newBlockEntity(IBlockReader worldIn){
		return new AutoCrafterTileEntity();
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		TileEntity te;
		if(!worldIn.isClientSide && (te = worldIn.getBlockEntity(pos)) instanceof AutoCrafterTileEntity){
			AutoCrafterTileEntity acTE = (AutoCrafterTileEntity) te;
			NetworkHooks.openGui((ServerPlayerEntity) playerIn, acTE, pos);
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos srcPos, boolean flag){
		if(!world.isClientSide){
			boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
			TileEntity te = world.getBlockEntity(pos);
			if(te instanceof AutoCrafterTileEntity){
				((AutoCrafterTileEntity) te).redstoneUpdate(powered);
			}
		}
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof AutoCrafterTileEntity) {
				((AutoCrafterTileEntity) te).dropItems();
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
