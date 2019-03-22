package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.tileentities.HopperFilterTileEntity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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

public class HopperFilter extends BlockContainer{

	protected HopperFilter(){
		super(Material.IRON);
		String name = "hopper_filter";
		setTranslationKey(name);
		setRegistryName(name);
		setHardness(2F);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setSoundType(SoundType.METAL);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new HopperFilterTileEntity();
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced){
		tooltip.add("Allows items to be moved through it only if they match the filter");
		tooltip.add("Doesn't move items on its own");
		tooltip.add("Setting a Shulker Box as a filter matches everything in the Shulker Box");
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face){
		return face.getAxis() == state.getValue(EssentialsProperties.AXIS) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	public static final AxisAlignedBB[][] BB = new AxisAlignedBB[][] {
			{new AxisAlignedBB(0, 0, 0, 0.25, 1, 1), new AxisAlignedBB(0.75, 0, 0, 1, 1, 1), new AxisAlignedBB(0.25, 0.1875, 0.1875, 0.75, 0.8125, 0.8125)},//X axis
			{new AxisAlignedBB(0, 0, 0, 1, 0.25, 1), new AxisAlignedBB(0, 0.75, 0, 1, 1, 1), new AxisAlignedBB(0.1875, 0.25, 0.1875, 0.8125, 0.75, 0.8125)},//Y axis
			{new AxisAlignedBB(0, 0, 0, 1, 1, 0.25), new AxisAlignedBB(0, 0, 0.75, 1, 1, 1), new AxisAlignedBB(0.1875, 0.1875, 0.25, 0.8125, 0.8125, 0.75)}//Z axis
	};

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean pleaseDontBeRelevantToAnythingOrIWillBeSad){
		EnumFacing.Axis axis = state.getValue(EssentialsProperties.AXIS);
		for(AxisAlignedBB bb : BB[axis.ordinal()]){
			addCollisionBoxToList(pos, mask, list, bb);
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
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.AXIS);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.AXIS).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(EssentialsProperties.AXIS, EnumFacing.Axis.values()[meta]);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand), worldIn.isRemote)){
			if(!worldIn.isRemote){
				worldIn.setBlockState(pos, state.cycleProperty(EssentialsProperties.AXIS));
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
						fte.setFilter(held.splitStack(1));
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
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return getDefaultState().withProperty(EssentialsProperties.AXIS, blockFaceClickedOn.getAxis());
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
}
