package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemChute extends Block{

	private static final VoxelShape[] BB = new VoxelShape[] {makeCuboidShape(0, .125D, .125D, 1, .875D, .875D), makeCuboidShape(.125D, 0, .125D, .875D, 1, .875D), makeCuboidShape(.125D, .125D, 0, .875D, .875D, 1)};
	
	protected ItemChute(){
		super(Properties.create(Material.IRON).hardnessAndResistance(1.5F).sound(SoundType.METAL));
		String name = "item_chute";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Nullable
	@Override
	public IBlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.AXIS, context.getFace().getAxis());
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				worldIn.setBlockState(pos, state.cycle(EssentialsProperties.AXIS));
			}
			return true;
		}
		return false;
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos){
		return BB[state.get(EssentialsProperties.AXIS).ordinal()];
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TextComponentString("Safe for decoration"));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder){
		builder.add(EssentialsProperties.AXIS);
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
		//Block updates are propogated down lines of Item Chutes, allowing caching of target positions for Item Shifters
		if(fromPos != null){
			EnumFacing.Axis axis = state.get(EssentialsProperties.AXIS);
			EnumFacing dir = EnumFacing.getFacingFromVector(pos.getX() - fromPos.getX(), pos.getY() - fromPos.getY(), pos.getZ() - fromPos.getZ());
			if(dir.getAxis() == axis){
				fromPos = pos;
				pos = pos.offset(dir);
				worldIn.getBlockState(pos).neighborChanged(worldIn, pos, this, fromPos);
			}
		}
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		return face.getAxis() == state.get(EssentialsProperties.AXIS) ? BlockFaceShape.CENTER_BIG : BlockFaceShape.UNDEFINED;
	}
}
