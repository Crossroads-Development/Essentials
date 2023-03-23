package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.api.WorldBuffer;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Notable differences from a normal piston include:
 * DIST_LIMIT blocks head range, distance controlled by signal strength,
 * No quasi-connectivity,
 * Piston extension takes 2-ticks per block extended, while retraction is instant. Block movement is not rendered
 * Can move up to PUSH_LIMIT blocks at a time instead of 12
 */
public class MultiPistonBase extends Block{

	private static final int DIST_LIMIT = 15;
	private static final int PUSH_LIMIT = 64;
	private static final int DELAY = RedstoneUtil.DELAY;
	private static final VoxelShape[] BB = new VoxelShape[] {box(0, 5, 0, 16, 16, 16), box(0, 0, 0, 16, 11, 16), box(0, 0, 5, 16, 16, 16), box(0, 0, 0, 16, 16, 11), box(5, 0, 0, 16, 16, 16), box(0, 0, 0, 11, 16, 16)};
	private final boolean sticky;

	/**
	 * Dirty shared field to prevent infinite loops from block updates triggering more block updates
	 * While true, a multipiston is actively changing the world around it and should ignore incoming blocks updates
	 */
	protected static boolean changingWorld = false;

	private static final BlockBehaviour.StatePredicate STATE_PREDICATE = (state, world, pos) -> !state.getValue(ESProperties.EXTENDED);

	protected MultiPistonBase(boolean sticky){
		super(Properties.of(Material.PISTON).isRedstoneConductor((state, world, pos) -> false).isSuffocating(STATE_PREDICATE).isViewBlocking(STATE_PREDICATE).strength(0.5F).sound(SoundType.METAL));
		String name = "multi_piston" + (sticky ? "_sticky" : "");
		this.sticky = sticky;
		registerDefaultState(defaultBlockState().setValue(ESProperties.FACING, Direction.NORTH).setValue(ESProperties.EXTENDED, false).setValue(ESProperties.SHIFTING, false));
		ESBlocks.toRegister.put(name, this);
		ESBlocks.blockAddQue(name, this);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.FACING, ESProperties.EXTENDED, ESProperties.SHIFTING);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		//Place with player orientation
		return defaultBlockState().setValue(ESProperties.FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.multi_piston.desc", DIST_LIMIT, PUSH_LIMIT));
		tooltip.add(Component.translatable("tt.essentials.multi_piston.reds"));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		//Extended multipistons have a different shape
		if(state.getValue(ESProperties.EXTENDED)){
			return BB[state.getValue(ESProperties.FACING).get3DDataValue()];
		}else{
			return Shapes.block();
		}
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving){
		BlockState otherState;
		//An internal change- due to this piston being in the process of extending/retracting (changing world) or due to this piston entering/leaving shifting state should not trigger the breaking of the extension
		if(!changingWorld && (newState.getBlock() != this || state.setValue(ESProperties.SHIFTING, false)
				!= newState.setValue(ESProperties.SHIFTING, false))){
			//Sanity check included to make sure the adjacent blocks is actually an extension- unlike vanilla pistons, multi pistons are supposed to actually work and not break bedrock
			if(state.getValue(ESProperties.EXTENDED) &&
					(otherState = world.getBlockState(pos.relative(state.getValue(ESProperties.FACING)))).getBlock() ==
							(sticky ? ESBlocks.multiPistonExtendSticky : ESBlocks.multiPistonExtend) &&
					otherState.getValue(ESProperties.AXIS) == state.getValue(ESProperties.FACING).getAxis()){
				//Break the multipiston head along this
				world.destroyBlock(pos.relative(state.getValue(ESProperties.FACING)), false);
			}
		}

		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		//Rotate with a wrench
		if(ConfigUtil.isWrench(playerIn.getItemInHand(hand)) && !state.getValue(ESProperties.EXTENDED) && !state.getValue(ESProperties.SHIFTING)){
			if(!worldIn.isClientSide){
				BlockState endState = state.cycle(ESProperties.FACING);//MCP note: cycle
				worldIn.setBlockAndUpdate(pos, endState);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state){
		//If extended or currently extending, this can not be moved. Otherwise it can be moved
		return state.getValue(ESProperties.EXTENDED) || state.getValue(ESProperties.SHIFTING) ? PushReaction.BLOCK : PushReaction.NORMAL;
	}

	@Override
	public void animateTick(BlockState state, Level worldIn, BlockPos pos, RandomSource rand){
		if(state.getValue(ESProperties.SHIFTING)){
			double particleRad = 0.75D;
			for(int i = 0; i < 4; i++){
				worldIn.addAlwaysVisibleParticle(ParticleTypes.SMOKE, pos.getX() + 0.5D + rand.nextGaussian() * particleRad, pos.getY() + 0.5D + rand.nextGaussian() * particleRad, pos.getZ() + 0.5D + rand.nextGaussian() * particleRad, 0, 0, 0);
			}
		}
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		neighborChanged(world.getBlockState(pos), world, pos, this, pos, false);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		if(worldIn.isClientSide || changingWorld || state.getValue(ESProperties.SHIFTING)){
			return;
		}

		//If incoming redstone signal would cause an extension different than current, schedule a change DELAY from now, and set shifting
		Direction facing = state.getValue(ESProperties.FACING);
		int redstone = getRedstoneInput(worldIn, pos, state, facing);
		int currExtend = getCurrentExtension(worldIn, pos, state, facing);

		if(redstone > currExtend){
			//Extension has a delay of DELAY ticks
			worldIn.setBlock(pos, state.setValue(ESProperties.SHIFTING, true), 2);
			worldIn.scheduleTick(pos, this, DELAY, TickPriority.NORMAL);
			playSound(worldIn, pos, false, true);
		}else{
			//Retraction happens instantly
			tick(state, (ServerLevel) worldIn, pos, worldIn.random);
		}
	}

	private int getRedstoneInput(Level world, BlockPos pos, BlockState state, Direction facing){
		int target = 0;

		for(Direction dir : Direction.values()){
			//Don't measure redstone power from the front, as otherwise we end up in an infinite loop just by placing a redstone blocks there
			if(dir != facing){
				target = Math.max(target, RedstoneUtil.getRedstoneOnSide(world, pos, dir));
			}
		}

//		This doesn't have any practical effect, as DIST_LIMIT is currently 15- but if it were changed, this line would be needed
//		target = Math.min(target, DIST_LIMIT);
		if(facing == Direction.UP){
			//Account for world height limits when pointed up
			target = Math.min(target, world.getMaxBuildHeight() - pos.getY() - 1);
		}else if(facing == Direction.DOWN){
			//Check for world floor
			target = Math.min(target, pos.getY() - world.getMinBuildHeight());
		}
		return target;
	}

	/**
	 * Plays a sound effect
	 * @param moving Whether this is the sound of actually moving
	 * @param extension Whether this is part of an extension (vs a retraction)
	 */
	private void playSound(Level world, BlockPos pos, boolean moving, boolean extension){
		if(moving){
			world.playSound(null, pos, extension ? SoundEvents.PISTON_EXTEND : SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.8F, 1);
		}else{
			world.playSound(null, pos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS, 1, 1);
		}
	}

	private int getCurrentExtension(Level world, BlockPos pos, BlockState state, Direction facing){
		int currentExtension = 0;

		if(state.getValue(ESProperties.EXTENDED)){
			BlockPos checkPos = pos.relative(facing);
			BlockState curState = world.getBlockState(checkPos);
			Block tarBlock = getExtensionBlock(sticky);

			//Find the current extension
			//The distance limit check is in case people mess around with setblock commands
			Direction.AxisDirection dir;
			while(curState.getBlock() == tarBlock && curState.getValue(ESProperties.AXIS) == facing.getAxis() && (dir = MultiPistonExtend.getDirFromHead(curState.getValue(ESProperties.HEAD))) != facing.getOpposite().getAxisDirection() && currentExtension != DIST_LIMIT){
				currentExtension++;
				checkPos = checkPos.relative(facing);
				curState = world.getBlockState(checkPos);
				if(dir != null){
					//Sanity check to make sure we don't have an invalid extension line
					break;
				}
			}
		}
		return currentExtension;
	}

	/**
	 * Gets the corresponding extension block
	 * @param sticky Whether this is the sticky variant
	 * @return The extension block type this block uses
	 */
	private Block getExtensionBlock(boolean sticky){
		if(sticky){
			return ESBlocks.multiPistonExtendSticky;
		}else{
			return ESBlocks.multiPistonExtend;
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand){
		//We re-check everything as the world could have changed in the previous 2 ticks
		Direction facing = state.getValue(ESProperties.FACING);
		int redstone = getRedstoneInput(world, pos, state, facing);
		int currExtend = getCurrentExtension(world, pos, state, facing);

		state = state.setValue(ESProperties.SHIFTING, false);
		world.setBlock(pos, state, 2);//Reset shifting state

		if(currExtend == redstone){
			return;//No change needed
		}

		changingWorld = true;
		WorldBuffer wBuf = new WorldBuffer(world);

		if(currExtend < redstone){
			//If we're extending, we do one block at a time, with a delay in between

			boolean blocked = shiftExtension(wBuf, pos, facing, currExtend, true);

			//If we are extending further, and we weren't blocked, queue another extension
			if(!blocked && currExtend + 1 < redstone){
				state = state.setValue(ESProperties.SHIFTING, true);
				world.setBlock(pos, state, 2);
				world.scheduleTick(pos, this, DELAY, TickPriority.NORMAL);
			}

			//Don't apply block updates until after all changes have been applied to avoid a variety of issues, including rail dupe bugs
			Set<BlockPos> toUpdate = wBuf.changedPositions();
			wBuf.applyChanges(1 | 2 | 64);//Flags used: 1: block update; 2: Send change to client; 64: isMoving
			for(BlockPos posToUpdate : toUpdate){
				world.updateNeighborsAt(posToUpdate, this);
			}

			if(!blocked){
				playSound(world, pos, true, true);
				state = state.setValue(ESProperties.EXTENDED, redstone != 0);
				world.setBlockAndUpdate(pos, state);
			}
		}else{
			//If we're retracting, we do it all at once, but calculate the result one block at a time

			for(int i = currExtend; i > redstone; i--){
				shiftExtension(wBuf, pos, facing, i, false);//Retraction is basically guaranteed
			}

			//Don't apply block updates until after all changes have been applied to avoid a variety of issues, including rail dupe bugs
			Set<BlockPos> toUpdate = wBuf.changedPositions();
			wBuf.applyChanges(1 | 2 | 64);//Flags used: 1: block update; 2: Send change to client; 64: isMoving
			for(BlockPos posToUpdate : toUpdate){
				world.updateNeighborsAt(posToUpdate, this);
			}

			playSound(world, pos, true, false);
			state = state.setValue(ESProperties.EXTENDED, redstone != 0);
			world.setBlockAndUpdate(pos, state);
		}

		changingWorld = false;
	}

	/**
	 * Adjusts the extension of the multipiston by one blocks in a WorldBuffer, and moves entities in the path
	 * @param world The WorldBuffer to read/write changes from
	 * @param pos The multipiston position
	 * @param facing The multipiston facing
	 * @param currentExtension The current extension of the multipiston (in the WorldBuffer)
	 * @param out Increase the extension if true, decrease otherwise
	 * @return true if this action was blocked
	 */
	private boolean shiftExtension(WorldBuffer world, BlockPos pos, Direction facing, int currentExtension, boolean out){
		Direction moveDir = out ? facing : facing.getOpposite();
		LinkedHashSet<BlockPos> movedBlocks = new LinkedHashSet<>(PUSH_LIMIT + 1);
		BlockPos prevHeadPos = pos.relative(facing, currentExtension);
		Block extendBlock = getExtensionBlock(sticky);
		if(!out){
			//Temporarily add the piston head to prevent it blocking movement paths
			movedBlocks.add(prevHeadPos);
		}
		//Only build a moveset if we are moving out or we are retracting with a sticky head
		if((out || sticky) && buildMoveset(pos, world, prevHeadPos.relative(facing), moveDir, movedBlocks, !out)){
			//Something is in the way
			if(!out){
				//If retracting, leave attached blocks behind but finish retracting
				movedBlocks.clear();
			}else{
				//If extending, stop here
				return true;
			}
		}
		if(!out){
			movedBlocks.remove(prevHeadPos);
		}

		//Change the current head
		if(currentExtension != 0){
			world.addChange(prevHeadPos, out ? extendBlock.defaultBlockState().setValue(ESProperties.AXIS, facing.getAxis()) : Blocks.AIR.defaultBlockState());
		}

		for(BlockPos changePos : movedBlocks){

			//Move blocks forward
			BlockState prevState = world.getBlockState(changePos);
			if(prevState.getPistonPushReaction() == PushReaction.DESTROY){
				world.getWorld().destroyBlock(changePos, true);//Destroy the blocks in the actual world to drop items
			}else{
				world.addChange(changePos.relative(moveDir), prevState);
			}
			world.addChange(changePos, Blocks.AIR.defaultBlockState());

			//Find and move entities
			//We use isSlimeBlock over isSticky as honey blocks aren't bouncy
			moveEnts(world.getWorld(), changePos, moveDir, prevState.isSlimeBlock());
		}

		//Find and move entities at the piston head itself
		moveEnts(world.getWorld(), prevHeadPos, moveDir, false);

		if(out){
			//Add the extended head
			world.addChange(pos.relative(facing, currentExtension + 1), extendBlock.defaultBlockState().setValue(ESProperties.AXIS, facing.getAxis()).setValue(ESProperties.HEAD, facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 2));
		}else if(currentExtension != 1){
			//Add the retracted head
			world.addChange(pos.relative(facing, currentExtension - 1), extendBlock.defaultBlockState().setValue(ESProperties.AXIS, facing.getAxis()).setValue(ESProperties.HEAD, facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 2));
		}

		return false;
	}

	private void moveEnts(Level world, BlockPos activePos, Direction moveDir, boolean sticky){
		AABB BB = new AABB(activePos.relative(moveDir));
		ArrayList<Entity> movingEnts = new ArrayList<>(4);
		getEntitiesMultiChunk(BB, world, movingEnts);
		for(Entity ent : movingEnts){
			if(ent.getPistonPushReaction() != PushReaction.IGNORE){
				ent.teleportTo(ent.getX() + (double) moveDir.getStepX(), ent.getY() + (double) moveDir.getStepY(), ent.getZ() + (double) moveDir.getStepZ());
				//If the entity is on a "sticky" blocks, bounce them
				if(sticky){
					ent.push(moveDir.getStepX(), moveDir.getStepY(), moveDir.getStepZ());
					ent.hurtMarked = true;
				}
			}
		}
		movingEnts.clear();
	}

	/**
	 * This method recursively builds a list of all blocks to be moved by the piston, and returns true if there is an obstacle blocking movement
	 *
	 * @param pistonPos Position of the piston
	 * @param world The WorldBuffer to read from
	 * @param curPos The currently active checking position
	 * @param moveDir The direction the piston is trying to move
	 * @param movedBlocks The current (in progress) movement set
	 * @param dragging Whether this blocks would be "dragged" instead of pushed
	 * @return If the movement has to be aborted
	 */
	private boolean buildMoveset(BlockPos pistonPos, WorldBuffer world, BlockPos curPos, Direction moveDir, LinkedHashSet<BlockPos> movedBlocks, boolean dragging){
		if(movedBlocks.contains(curPos)){
			return false;
		}
		BlockState state = world.getBlockState(curPos);

		//While vanilla has this very good and logical system for blocks exposing how they react to pistons, there is for no necessary reason a large number of exceptions
		PushReaction reaction;
		if(pistonPos.equals(curPos)){
			reaction = PushReaction.BLOCK;
		}else{
			reaction = getActualPushReaction(world, curPos, state);
		}

		boolean blocked = false;
		switch(reaction){
			case PUSH_ONLY:
				if(dragging){
					break;
				}
				//else treat push-only as normal
			case NORMAL:
				//Check for world height
				if(moveDir == Direction.UP && curPos.getY() == world.getWorld().getMaxBuildHeight() || moveDir == Direction.DOWN && curPos.getY() == world.getWorld().getMinBuildHeight()){
					blocked = true;
					break;
				}

				//The curPos needs to be added BEFORE recursively building a moveset to ensure it's counted in recursive push limit checks
				movedBlocks.add(curPos);
				blocked = movedBlocks.size() > PUSH_LIMIT || buildMoveset(pistonPos, world, curPos.relative(moveDir), moveDir, movedBlocks, false);
				//Crazy as it seems, it is necessary to remove and then re-add this position. This moves the curPos to the end of the movedBlocks.
				movedBlocks.remove(curPos);
				movedBlocks.add(curPos);

				//Do blocks behind this if sticky & can stick to (honey & slime don't stick together)
				if(state.isStickyBlock() && state.canStickTo(world.getBlockState(curPos.relative(moveDir.getOpposite())))){
					blocked = blocked || buildMoveset(pistonPos, world, curPos.relative(moveDir.getOpposite()), moveDir, movedBlocks, true);
				}

				//Do blocks son the sides if sticky
				if(state.isStickyBlock()){
					for(Direction side : Direction.values()){
						//Check this is on the sides, and that it is something it can actually stick to
						if(side.getAxis() != moveDir.getAxis() && state.canStickTo(world.getBlockState(curPos.relative(side)))){
							blocked = blocked || buildMoveset(pistonPos, world, curPos.relative(side), moveDir, movedBlocks, true);
						}
					}
				}

				break;
			case DESTROY:
				if(!dragging){
					movedBlocks.add(curPos);
				}
				break;
			case BLOCK:
				blocked = !dragging;
				break;
			case IGNORE:
				break;
		}

		return blocked || movedBlocks.size() > PUSH_LIMIT;
	}
	
	public static PushReaction getActualPushReaction(BlockGetter world, BlockPos pos, BlockState state){
		PushReaction reaction = state.getPistonPushReaction();
		if(state.isAir()){
			reaction = PushReaction.IGNORE;//Vanilla marks air as normal. This is an impressively stupid decision- it means we have to special case it
		}else if(state.getBlock() == Blocks.OBSIDIAN || state.getBlock() instanceof EntityBlock){
			reaction = PushReaction.BLOCK;//Guess what else is marked as normal? That's right, obsidian. You know, the quintessential unmovable blocks. It's special cased. whhhhyyyyyyyyyy?
		}else if(state.getDestroySpeed(world, pos) < 0){
			reaction = PushReaction.BLOCK;//Mod makers adding indestructible blocks regularly forget to make them immovable
		}else if(state.getBlock() instanceof PistonBaseBlock && state.hasProperty(PistonBaseBlock.EXTENDED)){
			reaction = state.getValue(PistonBaseBlock.EXTENDED) ? PushReaction.BLOCK : PushReaction.NORMAL;//Vanilla pistons report BLOCK even when retracted and movable
		}
		return reaction;
	}

	/**
	 * An alternate version of World#getEntitiesWithinAABBExcludingEntity that checks a 3x3x3 cube of mini chunks (16x16x16 cubes within chunks) for entities.
	 * This is less efficient than the standard method, but necessary to fix a vanilla bug whereby repeated getEntity calls break if entities were moved between chunks in the same tick.
	 */
	private static void getEntitiesMultiChunk(AABB checkBox, Level worldIn, ArrayList<Entity> entList){
		entList.addAll(worldIn.getEntities(null, checkBox));
		//As of MC1.17, the way entities are fetched from the world has changed
		//It seems to be that the underlying issue is fixed, and a simple method call is sufficient
//		final double maxEntityRad = worldIn.getMaxEntityRadius();
//		int i = Mth.floor((checkBox.minX - maxEntityRad) / 16.0D) - 1;
//		int j = Mth.floor((checkBox.maxX + maxEntityRad) / 16.0D) + 1;
//		int k = Mth.floor((checkBox.minZ - maxEntityRad) / 16.0D) - 1;
//		int l = Mth.floor((checkBox.maxZ + maxEntityRad) / 16.0D) + 1;
//
//		int yMin = Mth.clamp(Mth.floor((checkBox.minY - maxEntityRad) / 16.0D) - 1, 0, 15);
//		int yMax = Mth.clamp(Mth.floor((checkBox.maxY + maxEntityRad) / 16.0D) + 1, 0, 15);
//
//		for(int iLoop = i; iLoop <= j; iLoop++){
//			for(int kLoop = k; kLoop <= l; kLoop++){
//				if(worldIn.hasChunk(iLoop, kLoop)){
//					LevelChunk chunk = worldIn.getChunkAt(new BlockPos(iLoop * 16, 100, kLoop * 16));
//					for(int yLoop = yMin; yLoop <= yMax; yLoop++){
//						if(!chunk.getEntitySections()[yLoop].isEmpty()){
//							for(Entity entity : chunk.getEntitySections()[yLoop]){
//								if(entity.getBoundingBox().intersects(checkBox)){
//									entList.add(entity);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
	}
}
