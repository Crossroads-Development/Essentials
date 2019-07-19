package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class AbstractTile extends ContainerBlock implements IWireConnect{

	private static final Properties PROP = Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0, 0).sound(SoundType.WOOD);

	protected AbstractTile(String name){
		super(PROP);
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		//Don't register an item form
	}

	private static final VoxelShape BB = makeCuboidShape(0, 0, 0, 16, 2, 16);

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player){
		return new ItemStack(EssentialsBlocks.wireCircuit, 1);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		return BB;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos){
		return func_220064_c(worldIn, pos.down());
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		if(!state.isValidPosition(worldIn, pos)){
			worldIn.destroyBlock(pos, true);
		}
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		return side.getAxis() != Direction.Axis.Y;
	}
}
