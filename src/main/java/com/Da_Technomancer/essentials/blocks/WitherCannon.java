package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.EssentialsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class WitherCannon extends Block{

	public WitherCannon(){
		super(Properties.create(Material.ROCK, MaterialColor.BLACK).hardnessAndResistance(50F, 1200F).sound(SoundType.STONE));
		String name = "wither_cannon";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
		setDefaultState(getDefaultState().with(EssentialsProperties.REDSTONE_BOOL, false));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(EssentialsProperties.FACING, context.getNearestLookingDirection());
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(EssentialsProperties.FACING).add(EssentialsProperties.REDSTONE_BOOL);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				BlockState endState = state.cycle(EssentialsProperties.FACING);
				worldIn.setBlockState(pos, endState);
			}
			return true;
		}
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt." + Essentials.MODID + ".wither_cannon"));
	}

	@Override
	public int tickRate(IWorldReader p_149738_1_){
		return 4;
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos srcPos, boolean flag){
		boolean powered = world.isBlockPowered(pos) || world.isBlockPowered(pos.up());
		boolean wasActive = state.get(EssentialsProperties.REDSTONE_BOOL);
		if(powered && !wasActive){
			world.getPendingBlockTicks().scheduleTick(pos, this, tickRate(world));
			world.setBlockState(pos, state.with(EssentialsProperties.REDSTONE_BOOL, true), 4);
		}else if(!powered && wasActive){
			world.setBlockState(pos, state.with(EssentialsProperties.REDSTONE_BOOL, false), 4);
		}

	}

	@Override
	public void tick(BlockState state, World world, BlockPos pos, Random rand){
		if(!world.isRemote){
			Direction dir = state.get(EssentialsProperties.FACING);
			BlockPos spawnPos = pos.offset(dir);
			WitherSkullEntity skull = new WitherSkullEntity(EntityType.WITHER_SKULL, world);
			skull.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, dir.getHorizontalAngle() + 180, dir.getYOffset() * -90);
			skull.accelerationX = dir.getXOffset() / 6D;
			skull.accelerationY = dir.getYOffset() / 6D;
			skull.accelerationZ = dir.getZOffset() / 6D;
			world.addEntity(skull);
		}
	}
}
