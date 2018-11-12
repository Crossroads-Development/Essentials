package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class Brazier extends BlockContainer{

	private static final AxisAlignedBB BB = new AxisAlignedBB(0, 0, 0, 1, .875D, 1);

	protected Brazier(){
		super(Material.ROCK);
		String name = "brazier";
		setUnlocalizedName(name);
		setHardness(2);
		setRegistryName(name);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new BrazierTileEntity();
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return BB;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos){
		IBlockState other = world.getBlockState(pos);
		if(other.getBlock() != this){
			return other.getLightValue(world, pos);
		}
		switch(state.getValue(EssentialsProperties.BRAZIER_CONTENTS)){
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
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn){
		int type = worldIn.getBlockState(pos).getValue(EssentialsProperties.BRAZIER_CONTENTS);
		if(type == 1){
			entityIn.extinguish();
		}else if(type == 2){
			entityIn.setFire(5);
		}

		super.onEntityWalk(worldIn, pos, entityIn);
	}

	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance){
		int type = worldIn.getBlockState(pos).getValue(EssentialsProperties.BRAZIER_CONTENTS);
		if(type != 1 && type != 2){
			super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
		}
		if(type == 2 && entityIn instanceof EntityItem){
			entityIn.setDead();
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof BrazierTileEntity){
			ItemStack out = ((BrazierTileEntity) te).useItem(playerIn.getHeldItem(hand));
			if(!out.equals(playerIn.getHeldItem(hand))){
				if(!worldIn.isRemote){
					playerIn.setHeldItem(hand, out);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
		return getDefaultState().withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0);
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.BRAZIER_CONTENTS);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(EssentialsProperties.BRAZIER_CONTENTS, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.BRAZIER_CONTENTS);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> list, @Nullable Entity entityIn, boolean p_185477_7_){
		addCollisionBoxToList(pos, entityBox, list, BB);
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
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		if(!world.isRemote){
			TileEntity te = world.getTileEntity(pos);
			if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)){
				ItemStack made = ItemStack.EMPTY;
				switch(state.getValue(EssentialsProperties.BRAZIER_CONTENTS)){
					case 3:
						made = new ItemStack(Blocks.COAL_BLOCK);
						break;
					case 4:
						made = new ItemStack(Blocks.GLOWSTONE);
						break;
					case 6:
						made = new ItemStack(Blocks.SOUL_SAND);
						break;
					case 7:
						made = new ItemStack(Items.POISONOUS_POTATO);
						break;
				}
				InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), made);
			}
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced){
		tooltip.add("Able to hold Water, Lava, Glowstone, Coal Blocks, and Soul Sand");
		tooltip.add("Can prevent fall damage with liquid, emit light with Glowstone/Coal/Lava, destroy dropped items with Lava, or block witch spawns with Soul Sand");
	}


	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer(){
		return BlockRenderLayer.CUTOUT;
	}
}
