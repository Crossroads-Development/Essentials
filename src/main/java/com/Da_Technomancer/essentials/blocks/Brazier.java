package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class Brazier extends BlockContainer{

	private static final VoxelShape SHAPE;

	static{
		SHAPE = Block.makeCuboidShape(0.0625D, 0, 0.0625D, 0.9375D, .875D, 0.9375D);
	}


	protected Brazier(){
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2));
		String name = "brazier";
		setRegistryName(name);
		setDefaultState(getDefaultState().with(EssentialsProperties.BRAZIER_CONTENTS, 0));
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new BrazierTileEntity();
	}

	@Override
	public VoxelShape getShape(IBlockState state, IBlockReader worldIn, BlockPos pos){
		return SHAPE;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public int getLightValue(IBlockState state, IWorldReader world, BlockPos pos){
		IBlockState other = world.getBlockState(pos);
		if(other.getBlock() != this){
			return other.getLightValue(world, pos);
		}
		switch(state.get(EssentialsProperties.BRAZIER_CONTENTS)){
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
		int type = worldIn.getBlockState(pos).get(EssentialsProperties.BRAZIER_CONTENTS);
		if(type == 1){
			entityIn.extinguish();
		}else if(type == 2){
			entityIn.setFire(5);
		}

		super.onEntityWalk(worldIn, pos, entityIn);
	}

	@Override
	public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance){
		int type = worldIn.getBlockState(pos).get(EssentialsProperties.BRAZIER_CONTENTS);
		if(type != 1 && type != 2){
			super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
		}
		if(type == 2 && entityIn instanceof EntityItem){
			entityIn.remove();
		}
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
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
	protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder){
		builder.add(EssentialsProperties.BRAZIER_CONTENTS);
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public void onPlayerDestroy(IWorld world, BlockPos pos, IBlockState state){
		if(!world.getWorld().isRemote){
			TileEntity te = world.getTileEntity(pos);
			if(te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent()){
				ItemStack made = ItemStack.EMPTY;
				switch(state.get(EssentialsProperties.BRAZIER_CONTENTS)){
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
				InventoryHelper.spawnItemStack(world.getWorld(), pos.getX(), pos.getY(), pos.getZ(), made);
			}
		}
		super.onPlayerDestroy(world, pos, state);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TextComponentString("Able to hold Water, Lava, Glowstone, Coal Blocks, and Soul Sand"));
		tooltip.add(new TextComponentString("Can prevent fall damage with liquid, emit light with Glowstone/Coal/Lava, destroy dropped items with Lava, or block witch spawns with Soul Sand"));
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}
}
