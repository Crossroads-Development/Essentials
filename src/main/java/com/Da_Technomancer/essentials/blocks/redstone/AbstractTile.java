package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.redstone.IWireConnect;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractTile extends BaseEntityBlock implements IWireConnect{

	private static final Properties PROP = Properties.of(Material.DECORATION).strength(0, 0).sound(SoundType.WOOD);

	protected AbstractTile(String name){
		this(name, false);
	}

	protected AbstractTile(String name, boolean hasItemForm){
		super(PROP);
		ESBlocks.queueForRegister(name, this, hasItemForm);
	}

	private static final VoxelShape BB = box(0, 0, 0, 16, 2, 16);

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player){
		return new ItemStack(ESBlocks.wireCircuit, 1);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return BB;
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos){
		return canSupportCenter(worldIn, pos.below(), Direction.UP);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		if(!state.canSurvive(worldIn, pos)){
			worldIn.destroyBlock(pos, true);
		}
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		return side.getAxis() != Direction.Axis.Y;
	}

	/**
	 * @return Whether this circuit requires quartz to make
	 */
	public boolean usesQuartz(){
		return false;
	}
}
