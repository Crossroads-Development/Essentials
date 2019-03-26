package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class HopperFilter extends BlockContainer{

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
		tooltip.add(new TextComponentString("Allows items to be moved through it only if they match the filter"));
		tooltip.add(new TextComponentString("Doesn't move items on its own"));
		tooltip.add(new TextComponentString("Setting a Shulker Box as a filter matches everything in the Shulker Box"));
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		return face.getAxis() == state.get(EssentialsProperties.AXIS) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	private static final VoxelShape[] BB = new VoxelShape[3];

	static{
		BB[0] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 0.25, 1, 1), VoxelShapes.combine(makeCuboidShape(0.75, 0, 0, 1, 1, 1), makeCuboidShape(0.25, 0.1875, 0.1875, 0.75, 0.8125, 0.8125), IBooleanFunction.OR), IBooleanFunction.OR);//X axis
		BB[1] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 1, 0.25, 1), VoxelShapes.combine(makeCuboidShape(0, 0.75, 0, 1, 1, 1), makeCuboidShape(0.1875, 0.25, 0.1875, 0.8125, 0.75, 0.8125), IBooleanFunction.OR), IBooleanFunction.OR);//Y axis
		BB[2] = VoxelShapes.combine(makeCuboidShape(0, 0, 0, 1, 1, 0.25), VoxelShapes.combine(makeCuboidShape(0, 0, 0.75, 1, 1, 1), makeCuboidShape(0.1875, 0.1875, 0.25, 0.8125, 0.8125, 0.75), IBooleanFunction.OR), IBooleanFunction.OR);//Z axis
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos){
		return BB[state.get(EssentialsProperties.AXIS).ordinal()];
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder){
		builder.add(EssentialsProperties.AXIS);
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
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
	public IBlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.AXIS, context.getFace().getAxis());
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
}
