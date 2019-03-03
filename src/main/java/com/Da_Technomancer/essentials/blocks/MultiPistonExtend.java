package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
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
		setDefaultState(getDefaultState().withProperty(EssentialsProperties.AXIS, EnumFacing.Axis.Y).withProperty(EssentialsProperties.HEAD, 0));
		EssentialsBlocks.toRegister.add(this);
	}

	@Nullable
	protected static EnumFacing.AxisDirection getDirFromHead(int head){
		if(head == 0){
			return null;
		}
		return EnumFacing.AxisDirection.values()[head - 1];
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune){
		if(!MultiPistonBase.changingWorld){
			Block piston = sticky ? EssentialsBlocks.multiPistonSticky : EssentialsBlocks.multiPiston;
			NonNullList<ItemStack> drops = NonNullList.create();
			piston.getDrops(drops, worldIn, pos, state, fortune);
			for(ItemStack s : drops){
				spawnAsEntity(worldIn, pos, s);
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if(!MultiPistonBase.changingWorld){
			EnumFacing.Axis axis = state.getValue(EssentialsProperties.AXIS);
			EnumFacing.AxisDirection dir = getDirFromHead(state.getValue(EssentialsProperties.HEAD));

			Block piston = sticky ? EssentialsBlocks.multiPistonSticky : EssentialsBlocks.multiPiston;

			for(EnumFacing.AxisDirection actDir : EnumFacing.AxisDirection.values()){
				//Don't try to interact in the side with the piston head
				if(actDir != dir){
					BlockPos adjPos = pos.offset(EnumFacing.getFacingFromAxis(actDir, axis));
					IBlockState adjState = world.getBlockState(adjPos);
					//Even though under normal usage this if statement should be a guaranteed true, we check anyway in case we ended up in a glitched state, either through a bug or a player using the setblock command
					if((adjState.getBlock() == piston && adjState.getValue(EssentialsProperties.FACING).getAxis() == axis) || (adjState.getBlock() == this && adjState.getValue(EssentialsProperties.AXIS) == axis)){
						world.setBlockToAir(adjPos);
					}
				}
			}
		}
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.AXIS, EssentialsProperties.HEAD);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(EssentialsProperties.HEAD, (meta & 12) >>> 2).withProperty(EssentialsProperties.AXIS, EnumFacing.Axis.values()[meta & 3]);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.AXIS).ordinal() | (state.getValue(EssentialsProperties.HEAD) << 2);
	}

	private static final AxisAlignedBB[] ROD_BB = new AxisAlignedBB[] {new AxisAlignedBB(0.001D, .375D, .375D, 0.999D, .625D, .625D), new AxisAlignedBB(.375D, 0.001D, .375D, .625D, 0.999D, .625D), new AxisAlignedBB(.375D, .375D, 0.001D, .625D, .625D, 0.999D)};
	private static final AxisAlignedBB[] HEAD_BB = new AxisAlignedBB[] {new AxisAlignedBB(0, 0, 0, 1, 5D / 16D, 1), new AxisAlignedBB(0, 11D / 16D, 0, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 1, 1, 5D / 16D), new AxisAlignedBB(0, 0, 11D / 16D, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 5D / 16D, 1, 1), new AxisAlignedBB(11D / 16D, 0, 0, 1, 1, 1)};

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean pleaseDontBeRelevantToAnythingOrIWillBeSad){
		EnumFacing.Axis axis = state.getValue(EssentialsProperties.AXIS);
		addCollisionBoxToList(pos, mask, list, ROD_BB[axis.ordinal()]);
		EnumFacing.AxisDirection dir = getDirFromHead(state.getValue(EssentialsProperties.HEAD));
		if(dir != null){
			addCollisionBoxToList(pos, mask, list, HEAD_BB[EnumFacing.getFacingFromAxis(dir, axis).getIndex()]);
		}
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState state, World worldIn, BlockPos pos, Vec3d start, Vec3d end){
		ArrayList<AxisAlignedBB> list = new ArrayList<>(2);
		addCollisionBoxToList(state, worldIn, BlockPos.ORIGIN, FULL_BLOCK_AABB, list, null, false);
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
		ArrayList<AxisAlignedBB> list = new ArrayList<>(2);
		addCollisionBoxToList(state, source, BlockPos.ORIGIN, FULL_BLOCK_AABB, list, null, false);

		EntityPlayer play = Minecraft.getMinecraft().player;
		float reDist = Minecraft.getMinecraft().playerController.getBlockReachDistance();
		Vec3d start = play.getPositionEyes(0F).subtract(pos.getX(), pos.getY(), pos.getZ());
		Vec3d end = start.add(play.getLook(0F).x * reDist, play.getLook(0F).y * reDist, play.getLook(0F).z * reDist);
		AxisAlignedBB out = BlockUtil.selectionRaytrace(list, start, end);
		return (out == null ? list.get(0) : out).offset(pos);
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

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		if(face.getAxis() == state.getValue(EssentialsProperties.AXIS)){
			EnumFacing.AxisDirection dir  = getDirFromHead(state.getValue(EssentialsProperties.HEAD));
			if(dir == null || face.getAxisDirection() != dir){
				return BlockFaceShape.MIDDLE_POLE_THIN;
			}
			return BlockFaceShape.SOLID;
		}
		return BlockFaceShape.UNDEFINED;
	}
}