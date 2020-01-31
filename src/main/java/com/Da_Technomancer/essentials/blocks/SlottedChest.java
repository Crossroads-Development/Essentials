package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.blocks.redstone.IReadable;
import com.Da_Technomancer.essentials.tileentities.SlottedChestTileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class SlottedChest extends ContainerBlock implements IReadable{

	protected SlottedChest(){
		super(Properties.create(Material.WOOD).hardnessAndResistance(2).sound(SoundType.WOOD));
		String name = "slotted_chest";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new SlottedChestTileEntity();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof SlottedChestTileEntity) {
				InventoryHelper.dropInventoryItems(worldIn, pos, ((SlottedChestTileEntity) te).iInv);
				worldIn.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(!worldIn.isRemote){
			TileEntity te = worldIn.getTileEntity(pos);
			if(te instanceof SlottedChestTileEntity){
				ItemStack[] filter = ((SlottedChestTileEntity) te).lockedInv;
				NetworkHooks.openGui((ServerPlayerEntity) playerIn, (SlottedChestTileEntity) te, (buf) -> {
					for(ItemStack lock : filter){
						buf.writeItemStack(lock);
					}
				});
			}
		}
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.slotted_chest.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.slotted_chest.quip").setStyle(ESConfig.TT_QUIP));
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState p_149740_1_){
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos){
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof SlottedChestTileEntity){
			float val = ((SlottedChestTileEntity) te).calcComparator() * 15F;
			val = MathHelper.floor(val * 14.0F) + (val > 0 ? 1 : 0);
			return (int) val;
		}
		return 0;

	}

	@Override
	public float read(World world, BlockPos pos, BlockState state){
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof SlottedChestTileEntity){
			return ((SlottedChestTileEntity) te).calcComparator() * 15F;
		}
		return 0;
	}
}
