package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MultiPistonExtend extends Block{

	private final boolean sticky;

	protected MultiPistonExtend(boolean sticky){
		super(Material.PISTON);
		this.sticky = sticky;
		String name = "multi_piston_extend" + (sticky ? "_sticky" : "");
		setTranslationKey(name);
		setRegistryName(name);
		setHardness(0.5F);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setDefaultState(getDefaultState().withProperty(EssentialsProperties.FACING, EnumFacing.UP).withProperty(EssentialsProperties.HEAD, false));
		EssentialsBlocks.toRegister.add(this);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if(world.isRemote){
			return;
		}

		if(world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING))).getBlock() == this && world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING))).getValue(EssentialsProperties.FACING) == state.getValue(EssentialsProperties.FACING)){
			world.setBlockState(pos.offset(state.getValue(EssentialsProperties.FACING)), Blocks.AIR.getDefaultState());
		}

		if(world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getBlock() == this && world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getValue(EssentialsProperties.FACING) == state.getValue(EssentialsProperties.FACING)){
			world.setBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite()), Blocks.AIR.getDefaultState());
		}else if(world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getBlock() == (sticky ? EssentialsBlocks.multiPistonSticky : EssentialsBlocks.multiPiston)){
			((MultiPistonBase) world.getBlockState(pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite())).getBlock()).safeBreak(world, pos.offset(state.getValue(EssentialsProperties.FACING).getOpposite()));
		}
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.FACING, EssentialsProperties.HEAD);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(EssentialsProperties.HEAD, (meta & 8) == 8).withProperty(EssentialsProperties.FACING, EnumFacing.byIndex(meta & 7));
	}


	private static final AxisAlignedBB[] ROD_BB = new AxisAlignedBB[] {new AxisAlignedBB(0, .375D, .375D, 1, .625D, .625D), new AxisAlignedBB(.375D, 0, .375D, .625D, 1, .625D), new AxisAlignedBB(.375D, .375D, 0, .625D, .625D, 1)};
	private static final AxisAlignedBB[] HEAD_BB = new AxisAlignedBB[] {new AxisAlignedBB(0, 0, 0, 1, 5D / 16D, 1), new AxisAlignedBB(0, 11D / 16D, 0, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 1, 1, 5D / 16D), new AxisAlignedBB(0, 0, 11D / 16D, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 5D / 16D, 1, 1), new AxisAlignedBB(11D / 16D, 0, 0, 1, 1, 1)};

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean pleaseDontBeRelevantToAnythingOrIWillBeSad){
		addCollisionBoxToList(pos, mask, list, ROD_BB[state.getValue(EssentialsProperties.FACING).getAxis().ordinal()]);
		if(state.getValue(EssentialsProperties.HEAD)){
			addCollisionBoxToList(pos, mask, list, HEAD_BB[state.getValue(EssentialsProperties.FACING).getIndex()]);
		}
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState state, World worldIn, BlockPos pos, Vec3d start, Vec3d end){
		ArrayList<AxisAlignedBB> list = new ArrayList<>();
		list.add(ROD_BB[state.getValue(EssentialsProperties.FACING).getAxis().ordinal()]);
		if(state.getValue(EssentialsProperties.HEAD)){
			list.add(HEAD_BB[state.getValue(EssentialsProperties.FACING).getIndex()]);
		}

		start = start.subtract(pos.getX(), pos.getY(), pos.getZ());
		end = end.subtract(pos.getX(), pos.getY(), pos.getZ());
		AxisAlignedBB out = BlockUtil.selectionRaytrace(list, start, end);
		if(out == null){
			return null;
		}else{
			RayTraceResult untransformed = out.calculateIntercept(start, end);
			return new RayTraceResult(untransformed.hitVec.add(pos.getX(), pos.getY(), pos.getZ()), untransformed.sideHit, pos);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World source, BlockPos pos){
		ArrayList<AxisAlignedBB> list = new ArrayList<>();
		list.add(ROD_BB[state.getValue(EssentialsProperties.FACING).getAxis().ordinal()]);
		if(state.getValue(EssentialsProperties.HEAD)){
			list.add(HEAD_BB[state.getValue(EssentialsProperties.FACING).getIndex()]);
		}

		EntityPlayer play = Minecraft.getMinecraft().player;
		float reDist = Minecraft.getMinecraft().playerController.getBlockReachDistance();
		Vec3d start = play.getPositionEyes(0F).subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
		Vec3d end = start.add(play.getLook(0F).x * reDist, play.getLook(0F).y * reDist, play.getLook(0F).z * reDist);
		AxisAlignedBB out = BlockUtil.selectionRaytrace(list, start, end);
		return (out == null ? list.get(0) : out).offset(pos);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.FACING).getIndex() | (state.getValue(EssentialsProperties.HEAD) ? 8 : 0);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state){
		return EnumPushReaction.BLOCK;
	}
}