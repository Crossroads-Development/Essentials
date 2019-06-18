package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new HopperFilterTileEntity();
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new StringTextComponent("Allows items to be moved through it only if they match the filter"));
		tooltip.add(new StringTextComponent("Doesn't move items on its own"));
		tooltip.add(new StringTextComponent("Setting a Shulker Box as a filter matches everything in the Shulker Box"));
	}

	private static final VoxelShape[] BB = new VoxelShape[3];

	static{
		BB[0] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 4, 16, 16), VoxelShapes.combine(makeCuboidShape(12, 0, 0, 16, 16, 16), makeCuboidShape(4, 4, 4, 12, 12, 12), IBooleanFunction.OR), IBooleanFunction.OR);//X axis
		BB[1] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 16, 4, 16), VoxelShapes.combine(makeCuboidShape(0, 12, 0, 16, 16, 16), makeCuboidShape(4, 4, 4, 12, 12, 12), IBooleanFunction.OR), IBooleanFunction.OR);//Y axis
		BB[2] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 16, 16, 4), VoxelShapes.combine(makeCuboidShape(0, 0, 12, 16, 16, 16), makeCuboidShape(4, 4, 4, 12, 12, 12), IBooleanFunction.OR), IBooleanFunction.OR);//Z axis
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return BB[state.get(EssentialsProperties.AXIS).ordinal()];
	}

	@Override
	public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(EssentialsProperties.AXIS);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				worldIn.setBlockState(pos, state.cycle(EssentialsProperties.AXIS));
				TileEntity te = worldIn.getTileEntity(pos);
				if(te instanceof HopperFilterTileEntity){
					((HopperFilterTileEntity) te).clearCache();
				}
			}
			return true;
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
				return true;
			}
		}
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.AXIS, context.getFace().getAxis());
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}
}
