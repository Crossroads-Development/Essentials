package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.Essentials;
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
		super(Block.Properties.create(Material.IRON).hardnessAndResistance(2).sound(SoundType.METAL));
		String name = "auto_crafter";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn){
		return new AutoCrafterTileEntity();
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		TileEntity te;
		if(!worldIn.isRemote && (te = worldIn.getTileEntity(pos)) instanceof AutoCrafterTileEntity){
			AutoCrafterTileEntity acTE = (AutoCrafterTileEntity) te;
			NetworkHooks.openGui((ServerPlayerEntity) playerIn, acTE, buf -> {buf.writeString(acTE.recipe == null ? "" : acTE.recipe.toString()); buf.writeBlockPos(pos);});
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos srcPos, boolean flag){
		if(!world.isRemote){
			boolean powered = world.isBlockPowered(pos) || world.isBlockPowered(pos.up());
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof AutoCrafterTileEntity){
				((AutoCrafterTileEntity) te).redstoneUpdate(powered);
			}
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof AutoCrafterTileEntity) {
				((AutoCrafterTileEntity) te).dropItems();
				worldIn.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt." + Essentials.MODID + ".auto_crafter_basic"));
		tooltip.add(new TranslationTextComponent("tt." + Essentials.MODID + ".auto_crafter_book"));

	}
}
