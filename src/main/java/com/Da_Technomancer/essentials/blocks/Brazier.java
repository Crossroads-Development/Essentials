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
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class Brazier extends ContainerBlock{

	private static final VoxelShape SHAPE;

	static{
		SHAPE = VoxelShapes.or(Block.makeCuboidShape(4, 0, 4, 12, 10, 12), Block.makeCuboidShape(1, 10, 1, 15, 14, 15));
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return SHAPE;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos){
		BlockState other = world.getBlockState(pos);
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
		if(type == 2 && entityIn instanceof ItemEntity){
			entityIn.remove();
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
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
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(EssentialsProperties.BRAZIER_CONTENTS);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te instanceof BrazierTileEntity) {
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
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), made);
				worldIn.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new StringTextComponent("Able to hold Water, Lava, Glowstone, Coal Blocks, and Soul Sand"));
		tooltip.add(new StringTextComponent("Can prevent fall damage with liquid, emit light with Glowstone/Coal/Lava, destroy dropped items with Lava, or blocks witch spawns with Soul Sand"));
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT;
	}
}
