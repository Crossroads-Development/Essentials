package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.ESConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.StateContainer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@ObjectHolder(Essentials.MODID)
public class WitherCannon extends Block{

	@ObjectHolder("cannon_skull")
	public static EntityType<CannonSkull> ENT_TYPE;

	protected WitherCannon(){
		super(Properties.create(Material.ROCK, MaterialColor.BLACK).hardnessAndResistance(50F, 1200F).sound(SoundType.STONE));
		String name = "wither_cannon";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
		setDefaultState(getDefaultState().with(ESProperties.REDSTONE_BOOL, false));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(ESProperties.FACING, context.getNearestLookingDirection());
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.FACING).add(ESProperties.REDSTONE_BOOL);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(ESConfig.isWrench(playerIn.getHeldItem(hand))){
			if(!worldIn.isRemote){
				BlockState endState = state.cycle(ESProperties.FACING);
				worldIn.setBlockState(pos, endState);
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt." + Essentials.MODID + ".wither_cannon"));
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos srcPos, boolean flag){
		boolean powered = world.isBlockPowered(pos) || world.isBlockPowered(pos.up());
		boolean wasActive = state.get(ESProperties.REDSTONE_BOOL);
		if(powered && !wasActive){
			world.getPendingBlockTicks().scheduleTick(pos, this, 4);
			world.setBlockState(pos, state.with(ESProperties.REDSTONE_BOOL, true), 4);
		}else if(!powered && wasActive){
			world.setBlockState(pos, state.with(ESProperties.REDSTONE_BOOL, false), 4);
		}
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand){
		Direction dir = state.get(ESProperties.FACING);
		BlockPos spawnPos = pos.offset(dir);
		WitherSkullEntity skull = new CannonSkull(ENT_TYPE, world);
		skull.setLocationAndAngles(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, dir.getHorizontalAngle() + 180, dir.getYOffset() * -90);
		skull.setMotion(dir.getXOffset() / 5F, dir.getYOffset() / 5F, dir.getZOffset() / 5F);
		skull.accelerationX = dir.getXOffset() / 20D;
		skull.accelerationY = dir.getYOffset() / 20D;
		skull.accelerationZ = dir.getZOffset() / 20D;
		world.addEntity(skull);
	}

	public static class CannonSkull extends WitherSkullEntity{

		private int lifespan = 60;

		public CannonSkull(EntityType<CannonSkull> type, World world){
			super(type, world);
		}

		@Override
		public void tick(){
			super.tick();
			if(!world.isRemote && lifespan-- <= 0){
				world.addOptionalParticle(ParticleTypes.SMOKE, getPosX(), getPosY(), getPosZ(), 0, 0, 0);
				remove();
			}
		}

		@Override
		public void writeAdditional(CompoundNBT nbt){
			super.writeAdditional(nbt);
			nbt.putInt("lifetime", lifespan);
		}

		@Override
		public void readAdditional(CompoundNBT nbt){
			super.readAdditional(nbt);
			lifespan = nbt.getInt("lifetime");
		}

		@Override
		protected void onImpact(RayTraceResult result){
			if(!world.isRemote){
				if(result.getType() == RayTraceResult.Type.ENTITY){
					Entity entity = ((EntityRayTraceResult) result).getEntity();
					entity.attackEntityFrom(DamageSource.MAGIC, 5F);

					if(entity instanceof LivingEntity){
						//Locked at normal difficulty duration, because this is a redstone component meant to be precise and utilized and not an evil monster that exists to stab you
						((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.WITHER, 200, 1));
					}
				}
				//Ignore mob griefing- always use Explosion.Mode.DESTROY
				world.createExplosion(this, getPosX(), getPosY(), getPosZ(), 2F, false, Explosion.Mode.BREAK);
				remove();
			}
		}

		@Override
		public IPacket<?> createSpawnPacket(){
			return NetworkHooks.getEntitySpawningPacket(this);
		}
	}
}
