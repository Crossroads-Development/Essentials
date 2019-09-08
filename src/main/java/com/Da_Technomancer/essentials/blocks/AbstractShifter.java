package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.tileentities.AbstractShifterTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public abstract class AbstractShifter extends ContainerBlock{

	protected AbstractShifter(String name){
		super(Properties.create(Material.IRON).hardnessAndResistance(2).sound(SoundType.METAL));
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot){
		return state.with(EssentialsProperties.FACING, rot.rotate(state.get(EssentialsProperties.FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn){
		return state.rotate(mirrorIn.toRotation(state.get(EssentialsProperties.FACING)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(EssentialsProperties.FACING);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(!worldIn.isRemote){
			TileEntity te = worldIn.getTileEntity(pos);
			if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
				worldIn.setBlockState(pos, state.cycle(EssentialsProperties.FACING));
				if(te instanceof AbstractShifterTileEntity){
					((AbstractShifterTileEntity) te).refreshCache();
				}
			}else if(te instanceof AbstractShifterTileEntity){
				NetworkHooks.openGui((ServerPlayerEntity) playerIn, (AbstractShifterTileEntity) te, pos);
			}
		}
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		TileEntity te = worldIn.getTileEntity(pos);
		if(te instanceof AbstractShifterTileEntity){
			((AbstractShifterTileEntity) te).refreshCache();
		}
	}
}
