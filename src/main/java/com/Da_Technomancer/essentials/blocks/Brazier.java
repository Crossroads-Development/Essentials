package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class Brazier extends ContainerBlock{

	private static final VoxelShape SHAPE;

	static{
		SHAPE = VoxelShapes.or(Block.box(4, 0, 4, 12, 10, 12), Block.box(1, 10, 1, 15, 14, 15));
	}

	protected Brazier(){
		super(AbstractBlock.Properties.of(Material.STONE).strength(2));
		String name = "brazier";
		setRegistryName(name);
		registerDefaultState(defaultBlockState().setValue(ESProperties.BRAZIER_CONTENTS, 0));
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader world){
		return new BrazierTileEntity();
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return SHAPE;
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	public int getLightValue(BlockState state, IBlockReader world, BlockPos pos){
		BlockState other = world.getBlockState(pos);
		if(other.getBlock() != this){
			return other.getLightValue(world, pos);
		}
		switch(state.getValue(ESProperties.BRAZIER_CONTENTS)){
			case 2:
			case 4:
				return 15;
			case 3:
				return 14;
			case 7:
				return 3;
			default:
				return 0;
		}
	}

	@Override
	public void stepOn(World worldIn, BlockPos pos, Entity entityIn){
		int type = worldIn.getBlockState(pos).getValue(ESProperties.BRAZIER_CONTENTS);
		if(type == 1){
			entityIn.clearFire();
		}else if(type == 2){
			entityIn.setSecondsOnFire(5);
		}

		super.stepOn(worldIn, pos, entityIn);
	}

	@Override
	public void fallOn(World worldIn, BlockPos pos, Entity entityIn, float fallDistance){
		int type = worldIn.getBlockState(pos).getValue(ESProperties.BRAZIER_CONTENTS);
		if(type != 1 && type != 2){
			super.fallOn(worldIn, pos, entityIn, fallDistance);
		}
		if(type == 2 && entityIn instanceof ItemEntity){
			entityIn.remove();
		}
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		TileEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof BrazierTileEntity){
			ItemStack out = ((BrazierTileEntity) te).useItem(playerIn.getItemInHand(hand));
			if(!out.equals(playerIn.getItemInHand(hand))){
				if(!worldIn.isClientSide){
					playerIn.setItemInHand(hand, out);
				}
				return ActionResultType.CONSUME;
			}
		}

		return ActionResultType.FAIL;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.BRAZIER_CONTENTS);
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof BrazierTileEntity) {
				ItemStack made = ItemStack.EMPTY;
				switch(state.getValue(ESProperties.BRAZIER_CONTENTS)){
					case 3:
						made = new ItemStack(Blocks.COAL_BLOCK);
						break;
					case 4:
						made = new ItemStack(Blocks.GLOWSTONE);
						break;
					case 6:
						made = new ItemStack(Blocks.SOUL_SAND);
						break;
				}
				InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), made);
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.brazier.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.brazier.purpose"));
	}

//	@Override
//	public BlockRenderLayer getRenderLayer(){
//		return BlockRenderLayer.CUTOUT;
//	}
}
