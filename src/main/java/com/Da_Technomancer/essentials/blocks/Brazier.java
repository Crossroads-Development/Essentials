package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.BrazierTileEntity;
import net.minecraft.block.*;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class Brazier extends BaseEntityBlock{

	private static final VoxelShape SHAPE;

	static{
		SHAPE = Shapes.or(Block.box(4, 0, 4, 12, 10, 12), Block.box(1, 10, 1, 15, 14, 15));
	}

	protected Brazier(){
		super(BlockBehaviour.Properties.of(Material.STONE).strength(2));
		String name = "brazier";
		setRegistryName(name);
		registerDefaultState(defaultBlockState().setValue(ESProperties.BRAZIER_CONTENTS, 0));
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Override
	public BlockEntity newBlockEntity(BlockGetter world){
		return new BrazierTileEntity();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return SHAPE;
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	public int getLightValue(BlockState state, BlockGetter world, BlockPos pos){
		BlockState other = world.getBlockState(pos);
		if(other.getBlock() != this){
			return other.getLightValue(world, pos);
		}
		switch(state.getValue(ESProperties.BRAZIER_CONTENTS)){
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
	public void stepOn(Level worldIn, BlockPos pos, Entity entityIn){
		int type = worldIn.getBlockState(pos).getValue(ESProperties.BRAZIER_CONTENTS);
		if(type == 1){
			entityIn.clearFire();
		}else if(type == 2){
			entityIn.setSecondsOnFire(5);
		}

		super.stepOn(worldIn, pos, entityIn);
	}

	@Override
	public void fallOn(Level worldIn, BlockPos pos, Entity entityIn, float fallDistance){
		int type = worldIn.getBlockState(pos).getValue(ESProperties.BRAZIER_CONTENTS);
		if(type != 1 && type != 2){
			super.fallOn(worldIn, pos, entityIn, fallDistance);
		}
		if(type == 2 && entityIn instanceof ItemEntity){
			entityIn.remove();
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof BrazierTileEntity){
			ItemStack out = ((BrazierTileEntity) te).useItem(playerIn.getItemInHand(hand));
			if(!out.equals(playerIn.getItemInHand(hand))){
				if(!worldIn.isClientSide){
					playerIn.setItemInHand(hand, out);
				}
				return InteractionResult.CONSUME;
			}
		}

		return InteractionResult.FAIL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.BRAZIER_CONTENTS);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof BrazierTileEntity) {
				ItemStack made = ItemStack.EMPTY;
				switch(state.getValue(ESProperties.BRAZIER_CONTENTS)){
					case 3:
						made = new ItemStack(Blocks.COAL_BLOCK);
						break;
					case 4:
						made = new ItemStack(Blocks.GLOWSTONE);
						break;
					case 6:
						made = new ItemStack(Blocks.SOUL_SAND);
						break;
				}
				Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), made);
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, worldIn, pos, newState, isMoving);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.brazier.desc"));
		tooltip.add(new TranslatableComponent("tt.essentials.brazier.purpose"));
	}

//	@Override
//	public BlockRenderLayer getRenderLayer(){
//		return BlockRenderLayer.CUTOUT;
//	}
}
