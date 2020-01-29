package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
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

public class HopperFilter extends ContainerBlock{

	protected HopperFilter(){
		super(Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(2));
		String name = "hopper_filter";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new HopperFilterTileEntity();
	}

//	@Override
//	public BlockRenderLayer getRenderLayer(){
//		return BlockRenderLayer.CUTOUT;
//	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.hopper_filter.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.hopper_filter.move"));
		tooltip.add(new TranslationTextComponent("tt.essentials.hopper_filter.shulker"));
	}

	private static final VoxelShape[] BB = new VoxelShape[3];

	static{
		BB[0] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 4, 16, 16), VoxelShapes.combine(makeCuboidShape(12, 0, 0, 16, 16, 16), makeCuboidShape(4, 4, 4, 12, 12, 12), IBooleanFunction.OR), IBooleanFunction.OR);//X axis
		BB[1] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 16, 4, 16), VoxelShapes.combine(makeCuboidShape(0, 12, 0, 16, 16, 16), makeCuboidShape(4, 4, 4, 12, 12, 12), IBooleanFunction.OR), IBooleanFunction.OR);//Y axis
		BB[2] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 16, 16, 4), VoxelShapes.combine(makeCuboidShape(0, 0, 12, 16, 16, 16), makeCuboidShape(4, 4, 4, 12, 12, 12), IBooleanFunction.OR), IBooleanFunction.OR);//Z axis
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return BB[state.get(ESProperties.AXIS).ordinal()];
	}

	@Override
	public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.AXIS);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof HopperFilterTileEntity) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), ((HopperFilterTileEntity) te).getFilter());
				worldIn.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(ESConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				worldIn.setBlockState(pos, state.cycle(ESProperties.AXIS));
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof HopperFilterTileEntity){
					((HopperFilterTileEntity) te).clearCache();
				}
			}
			return ActionResultType.SUCCESS;
		}else{
			TileEntity te = worldIn.getTileEntity(pos);
			if(te instanceof HopperFilterTileEntity){
				if(!worldIn.isRemote){
					HopperFilterTileEntity fte = (HopperFilterTileEntity) te;
					ItemStack held = playerIn.getHeldItem(hand);
					if(fte.getFilter().isEmpty() && !held.isEmpty()){
						fte.setFilter(held.split(1));
						playerIn.setHeldItem(hand, held);
					}else if(!fte.getFilter().isEmpty() && held.isEmpty()){
						playerIn.setHeldItem(hand, fte.getFilter());
						fte.setFilter(ItemStack.EMPTY);
					}
				}
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.FAIL;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(ESProperties.AXIS, context.getFace().getAxis());
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}
