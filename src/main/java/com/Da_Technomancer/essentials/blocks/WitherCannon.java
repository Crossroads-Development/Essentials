package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.Player;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.BlockPlaceContext ;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.state.StateDefinition;
import net.minecraft.util.InteractionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockHitResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;
import net.minecraft.world.server.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import net.minecraft.block.AbstractBlock.Properties;

@ObjectHolder(Essentials.MODID)
public class WitherCannon extends Block{

	@ObjectHolder("cannon_skull")
	public static EntityType<CannonSkull> ENT_TYPE;

	protected WitherCannon(){
		super(Properties.of(Material.STONE, MaterialColor.COLOR_BLACK).strength(50F, 1200F).sound(SoundType.STONE));
		String name = "wither_cannon";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
		registerDefaultState(defaultBlockState().setValue(ESProperties.REDSTONE_BOOL, false));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext  context){
		return defaultBlockState().setValue(ESProperties.FACING, context.getNearestLookingDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.FACING).add(ESProperties.REDSTONE_BOOL);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		if(ESConfig.isWrench(playerIn.getItemInHand(hand))){
			if(!worldIn.isClientSide){
				BlockState endState = state.cycle(ESProperties.FACING);//MCP note: cycle
				worldIn.setBlockAndUpdate(pos, endState);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.wither_cannon"));
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos srcPos, boolean flag){
		boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
		boolean wasActive = state.getValue(ESProperties.REDSTONE_BOOL);
		if(powered && !wasActive){
			world.getBlockTicks().scheduleTick(pos, this, 4);
			world.setBlock(pos, state.setValue(ESProperties.REDSTONE_BOOL, true), 4);
		}else if(!powered && wasActive){
			world.setBlock(pos, state.setValue(ESProperties.REDSTONE_BOOL, false), 4);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, Random rand){
		Direction dir = state.getValue(ESProperties.FACING);
		BlockPos spawnPos = pos.relative(dir);
		WitherSkullEntity skull = new CannonSkull(ENT_TYPE, world);
		skull.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, dir.toYRot() + 180, dir.getStepY() * -90);
		skull.setDeltaMovement(dir.getStepX() / 5F, dir.getStepY() / 5F, dir.getStepZ() / 5F);
		skull.xPower = dir.getStepX() / 20D;
		skull.yPower = dir.getStepY() / 20D;
		skull.zPower = dir.getStepZ() / 20D;
		world.addFreshEntity(skull);
	}

	public static class CannonSkull extends WitherSkullEntity{

		private int lifespan = 60;

		public CannonSkull(EntityType<CannonSkull> type, Level world){
			super(type, world);
		}

		@Override
		public void tick(){
			super.tick();
			if(!level.isClientSide && lifespan-- <= 0){
				level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
				remove();
			}
		}

		@Override
		public void addAdditionalSaveData(CompoundNBT nbt){
			super.addAdditionalSaveData(nbt);
			nbt.putInt("lifetime", lifespan);
		}

		@Override
		public void readAdditionalSaveData(CompoundNBT nbt){
			super.readAdditionalSaveData(nbt);
			lifespan = nbt.getInt("lifetime");
		}

		@Override
		protected void onHit(RayTraceResult result){
			if(!level.isClientSide){
				if(result.getType() == RayTraceResult.Type.ENTITY){
					Entity entity = ((EntityRayTraceResult) result).getEntity();
					entity.hurt(DamageSource.MAGIC, 5F);

					if(entity instanceof LivingEntity){
						//Locked at normal difficulty duration, because this is a redstone component meant to be precise and utilized and not an evil monster that exists to stab you
						((LivingEntity) entity).addEffect(new EffectInstance(Effects.WITHER, 200, 1));
					}
				}
				//Ignore mob griefing- always use Explosion.Mode.DESTROY
				level.explode(this, getX(), getY(), getZ(), 2F, false, Explosion.Mode.BREAK);
				remove();
			}
		}

		@Override
		public IPacket<?> getAddEntityPacket(){
			return NetworkHooks.getEntitySpawningPacket(this);
		}
	}
}
