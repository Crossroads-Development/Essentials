package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@ObjectHolder(Essentials.MODID)
public class WitherCannon extends Block{

	@ObjectHolder("cannon_skull")
	public static EntityType<CannonSkull> ENT_TYPE;

	protected WitherCannon(){
		super(ESBlocks.getRockProperty().strength(50F, 1200F));
		String name = "wither_cannon";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
		registerDefaultState(defaultBlockState().setValue(ESProperties.REDSTONE_BOOL, false));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
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
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.wither_cannon"));
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos srcPos, boolean flag){
		boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(pos.above());
		boolean wasActive = state.getValue(ESProperties.REDSTONE_BOOL);
		if(powered && !wasActive){
			world.scheduleTick(pos, this, 4);
			world.setBlock(pos, state.setValue(ESProperties.REDSTONE_BOOL, true), 4);
		}else if(!powered && wasActive){
			world.setBlock(pos, state.setValue(ESProperties.REDSTONE_BOOL, false), 4);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, Random rand){
		Direction dir = state.getValue(ESProperties.FACING);
		BlockPos spawnPos = pos.relative(dir);
		WitherSkull skull = new CannonSkull(ENT_TYPE, world);
		skull.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, dir.toYRot() + 180, dir.getStepY() * -90);
		skull.setDeltaMovement(dir.getStepX() / 5F, dir.getStepY() / 5F, dir.getStepZ() / 5F);
		skull.xPower = dir.getStepX() / 20D;
		skull.yPower = dir.getStepY() / 20D;
		skull.zPower = dir.getStepZ() / 20D;
		world.addFreshEntity(skull);
	}

	public static class CannonSkull extends WitherSkull{

		private int lifespan = 60;

		public CannonSkull(EntityType<CannonSkull> type, Level world){
			super(type, world);
		}

		@Override
		public void tick(){
			super.tick();
			if(!level.isClientSide && lifespan-- <= 0){
				level.addAlwaysVisibleParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
				remove(RemovalReason.DISCARDED);
			}
		}

		@Override
		public void addAdditionalSaveData(CompoundTag nbt){
			super.addAdditionalSaveData(nbt);
			nbt.putInt("lifetime", lifespan);
		}

		@Override
		public void readAdditionalSaveData(CompoundTag nbt){
			super.readAdditionalSaveData(nbt);
			lifespan = nbt.getInt("lifetime");
		}

		@Override
		protected void onHit(HitResult result){
			if(!level.isClientSide){
				if(result.getType() == HitResult.Type.ENTITY){
					Entity entity = ((EntityHitResult) result).getEntity();
					entity.hurt(DamageSource.MAGIC, 5F);

					if(entity instanceof LivingEntity){
						//Locked at normal difficulty duration, because this is a redstone component meant to be precise and utilized and not an evil monster that exists to stab you
						((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
					}
				}
				//Ignore mob griefing- always use Explosion.Mode.DESTROY
				level.explode(this, getX(), getY(), getZ(), 2F, false, Explosion.BlockInteraction.BREAK);
				remove(RemovalReason.DISCARDED);
			}
		}

		@Override
		public Packet<?> getAddEntityPacket(){
			return NetworkHooks.getEntitySpawningPacket(this);
		}
	}
}
