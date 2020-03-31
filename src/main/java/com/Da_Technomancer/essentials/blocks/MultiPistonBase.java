package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.WorldBuffer;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Notable differences from a normal piston include:
 * DIST_LIMIT blocks head range, distance controlled by signal strength,
 * No quasi-connectivity,
 * Redstone can be placed on top of the piston,
 * Piston extension and retraction is instant, no 2-tick delay or rendering of blocks movement.
 * Can move up to PUSH_LIMIT blocks at a time instead of 12
 */
public class MultiPistonBase extends Block{

	private static final int DIST_LIMIT = 15;
	private static final int PUSH_LIMIT = 64;
	private final boolean sticky;

	protected MultiPistonBase(boolean sticky){
		super(Properties.create(Material.PISTON).hardnessAndResistance(0.5F).sound(SoundType.METAL));
		String name = "multi_piston" + (sticky ? "_sticky" : "");
		setRegistryName(name);
		this.sticky = sticky;
		setDefaultState(getDefaultState().with(ESProperties.FACING, Direction.NORTH).with(ESProperties.EXTENDED, false));
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context){
		return getDefaultState().with(ESProperties.FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		if(ESConfig.isWrench(playerIn.getHeldItem(hand)) && !state.get(ESProperties.EXTENDED)){
			if(!worldIn.isRemote){
				BlockState endState = state.cycle(ESProperties.FACING);
				worldIn.setBlockState(pos, endState);
			}
			return true;
		}
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new StringTextComponent(String.format("An uber piston that can push up to %1$d blocks away, can move %2$d blocks at a time, and extends instantly", DIST_LIMIT, PUSH_LIMIT)));
		tooltip.add(new StringTextComponent("Extension length controlled by signal strength"));
	}

	private static final VoxelShape[] BB = new VoxelShape[] {makeCuboidShape(0, 5, 0, 16, 16, 16), makeCuboidShape(0, 0, 0, 16, 11, 16), makeCuboidShape(0, 0, 5, 16, 16, 16), makeCuboidShape(0, 0, 0, 16, 16, 11), makeCuboidShape(5, 0, 0, 16, 16, 16), makeCuboidShape(0, 0, 0, 11, 16, 16)};

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
		if(state.get(ESProperties.EXTENDED)){
			return BB[state.get(ESProperties.FACING).getIndex()];
		}else{
			return VoxelShapes.fullCube();
		}
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving){	BlockState otherState;
		//Sanity check included to make sure the adjacent blocks is actually an extension- unlike vanilla pistons, multi pistons are supposed to actually work and not break bedrock
		if(state.get(ESProperties.EXTENDED) && (otherState = world.getBlockState(pos.offset(state.get(ESProperties.FACING)))).getBlock() == (sticky ? ESBlocks.multiPistonExtendSticky : ESBlocks.multiPistonExtend) && otherState.get(ESProperties.AXIS) == state.get(ESProperties.FACING).getAxis()){
			world.destroyBlock(pos.offset(state.get(ESProperties.FACING)), false);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		neighborChanged(world.getBlockState(pos), world, pos, this, pos, false);
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side){
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean flag){
		if(worldIn.isRemote || changingWorld){
			return;
		}
		activate(worldIn, pos, state);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.FACING, ESProperties.EXTENDED);
	}

	@Override
	public PushReaction getPushReaction(BlockState state){
		//If extended, this can not be moved. Otherwise it can be moved
		return state.get(ESProperties.EXTENDED) ? PushReaction.BLOCK : PushReaction.NORMAL;
	}

	//While true, a multipiston is actively changing the world around it and should ignore incoming blocks updates
	protected static boolean changingWorld = false;

	private void activate(World world, BlockPos pos, BlockState state){
		int target = 0;

		Direction facing = state.get(ESProperties.FACING);

		for(Direction dir : Direction.values()){
			//Don't measure redstone power from the front, as otherwise we end up in an infinite loop just by placing a redstone blocks there
			if(dir != facing){
				target = Math.max(target, world.getRedstonePower(pos.offset(dir), dir));
			}
		}

		//A fairly common bug in mods and newly added vanilla blocks is ways to get redstone signal strengths over 15.
		target = Math.min(target, DIST_LIMIT);

		if(facing == Direction.UP){
			//Account for world height limits when pointed up
			target = Math.min(target, world.getHeight() - pos.getY() - 1);
		}else if(facing == Direction.DOWN){
			//Check for world floor
			target = Math.min(target, pos.getY());
		}

		int currentExtension = 0;

		if(state.get(ESProperties.EXTENDED)){
			BlockPos checkPos = pos.offset(facing);
			BlockState curState = world.getBlockState(checkPos);
			Block tarBlock = sticky ? ESBlocks.multiPistonExtendSticky : ESBlocks.multiPistonExtend;

			//Find the current extension
			//The distance limit check is in case people mess around with setblock commands
			Direction.AxisDirection dir;
			while(curState.getBlock() == tarBlock && curState.get(ESProperties.AXIS) == facing.getAxis() && (dir = MultiPistonExtend.getDirFromHead(curState.get(ESProperties.HEAD))) != facing.getOpposite().getAxisDirection() && currentExtension != DIST_LIMIT){
				currentExtension++;
				checkPos = checkPos.offset(facing);
				curState = world.getBlockState(checkPos);
				if(dir != null){
					//Sanity check to make sure we don't have an invalid extension line
					break;
				}
			}
		}

		if(currentExtension == target){
			//No work needs to be done
			return;
		}

		changingWorld = true;

		WorldBuffer wBuf = new WorldBuffer(world);

		if(currentExtension < target){
			for(int i = currentExtension; i < target; i++){
				if(shiftExtension(wBuf, pos, facing, i, true)){
					target = i;
					break;
				}
			}
		}else{
			for(int i = currentExtension; i > target; i--){
				shiftExtension(wBuf, pos, facing, i, false);
			}
		}

		//Don't apply block updates until after all changes have been applied to avoid a variety of issues, including rail dupe bugs
		Set<BlockPos> toUpdate = wBuf.changedPositions();
		wBuf.applyChanges(1 | 2 | 64);//Flags used: 1: block update; 2: Send change to client; 64: isMoving
//		for(BlockPos posToUpdate : toUpdate){
//			world.notifyNeighbors(posToUpdate, this);
//		}

		if(currentExtension == 0 ^ target == 0){
			world.setBlockState(pos, state.with(ESProperties.EXTENDED, target != 0));
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
		BlockPos prevHeadPos = pos.offset(facing, currentExtension);
		Block extendBlock = sticky ? ESBlocks.multiPistonExtendSticky : ESBlocks.multiPistonExtend;
		if(!out){
			//Temporarily add the piston head to prevent it blocking movement paths
			movedBlocks.add(prevHeadPos);
		}
		//Only build a moveset if we are moving out or we are retracting with a sticky head
		if((out || sticky) && buildMoveset(pos, world, prevHeadPos.offset(facing), moveDir, movedBlocks, !out)){
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
			world.addChange(prevHeadPos, out ? extendBlock.getDefaultState().with(ESProperties.AXIS, facing.getAxis()) : Blocks.AIR.getDefaultState());
		}

		for(BlockPos changePos : movedBlocks){

			//Move blocks forward
			BlockState prevState = world.getBlockState(changePos);
			if(prevState.getPushReaction() == PushReaction.DESTROY){
				world.getWorld().destroyBlock(changePos, true);//Destroy the blocks in the actual world to drop items
			}else{
				world.addChange(changePos.offset(moveDir), prevState);
			}
			world.addChange(changePos, Blocks.AIR.getDefaultState());

			//Find and move entities
			moveEnts(world.getWorld(), changePos, moveDir, isStickyBlock(prevState));
		}

		//Find and move entities at the piston head itself
		moveEnts(world.getWorld(), prevHeadPos, moveDir, false);

		if(out){
			//Add the extended head
			world.addChange(pos.offset(facing, currentExtension + 1), extendBlock.getDefaultState().with(ESProperties.AXIS, facing.getAxis()).with(ESProperties.HEAD, facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 2));
		}else if(currentExtension != 1){
			//Add the retracted head
			world.addChange(pos.offset(facing, currentExtension - 1), extendBlock.getDefaultState().with(ESProperties.AXIS, facing.getAxis()).with(ESProperties.HEAD, facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : 2));
		}

		return false;
	}

	private void moveEnts(World world, BlockPos activePos, Direction moveDir, boolean sticky){
		AxisAlignedBB BB = new AxisAlignedBB(activePos.offset(moveDir));
		ArrayList<Entity> movingEnts = new ArrayList<>(4);
		getEntitiesMultiChunk(BB, world, movingEnts);
		for(Entity ent : movingEnts){
			if(ent.getPushReaction() != PushReaction.IGNORE){
				ent.setPositionAndUpdate(ent.posX + (double) moveDir.getXOffset(), ent.posY + (double) moveDir.getYOffset(), ent.posZ + (double) moveDir.getZOffset());
				//If the entity is on a "sticky" blocks, bounce them
				if(sticky){
					ent.addVelocity(moveDir.getXOffset(), moveDir.getYOffset(), moveDir.getZOffset());
					ent.velocityChanged = true;
				}
			}
		}
		movingEnts.clear();
	}

	/**
	 * @param pistonPos Position of the piston
	 * @param world The WorldBuffer to read from
	 * @param curPos The currently active checking position
	 * @param moveDir The direction the piston is trying to move
	 * @param movedBlocks The current (in progress) movement set
	 * @param dragging Whether this blocks would be "dragged" instead of pushed
	 * @return If the movement has to be aborted
	 *
	 * This method recursively builds a list of all blocks to be moved by the piston, and returns true if there is an obstacle blocking movement
	 */
	private boolean buildMoveset(BlockPos pistonPos, WorldBuffer world, BlockPos curPos, Direction moveDir, LinkedHashSet<BlockPos> movedBlocks, boolean dragging){
		if(movedBlocks.contains(curPos)){
			return false;
		}
		BlockState state = world.getBlockState(curPos);

		//While vanilla has this very good and logical system for blocks exposing how they react to pistons, there is for no necessary reason a large number of exceptions
		PushReaction reaction = state.getPushReaction();
		if(state.getBlock().isAir(state, world.getWorld(), curPos)){
			reaction = PushReaction.IGNORE;//Vanilla marks air as normal. This is an impressively stupid decision- it means we have to special case it
		}else if(state.getBlock() == Blocks.OBSIDIAN || state.getBlock().hasTileEntity(state) || pistonPos.equals(curPos)){
			reaction = PushReaction.BLOCK;//Guess what else is marked as normal? That's right, obsidian. You know, the quintessential unmovable blocks. It's special cased. whhhhyyyyyyyyyy?
		}else if(state.getBlockHardness(world, curPos) < 0){
			reaction = PushReaction.BLOCK;//Mod makers adding indestructible blocks regularly forget to make them immovable
		}else if(state.getBlock() instanceof PistonBlock && state.has(PistonBlock.EXTENDED)){
			reaction = state.get(PistonBlock.EXTENDED) ? PushReaction.BLOCK : PushReaction.NORMAL;//Vanilla pistons report BLOCK even when retracted and movable
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
				if(moveDir == Direction.UP && curPos.getY() == world.getWorld().getHeight() || moveDir == Direction.DOWN && curPos.getY() == 0){
					blocked = true;
					break;
				}

				//The curPos needs to be added BEFORE recursively building a moveset to ensure it's counted in recursive push limit checks
				movedBlocks.add(curPos);
				blocked = movedBlocks.size() > PUSH_LIMIT || buildMoveset(pistonPos, world, curPos.offset(moveDir), moveDir, movedBlocks, false);
				//Crazy as it seems, it is necessary to remove and then re-add this position. This moves the curPos to the end of the movedBlocks.
				movedBlocks.remove(curPos);
				movedBlocks.add(curPos);

				//Do blocks behind this if sticky
				if(isStickyBlock(state)){
					blocked = blocked || buildMoveset(pistonPos, world, curPos.offset(moveDir.getOpposite()), moveDir, movedBlocks, true);
				}

				//Do blocks son the sides if sticky
				if(isStickyBlock(state)){
					for(Direction side : Direction.values()){
						if(side.getAxis() != moveDir.getAxis()){
							blocked = blocked || buildMoveset(pistonPos, world, curPos.offset(side), moveDir, movedBlocks, true);
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

	/**
	 * An alternate version of World#getEntitiesWithinAABBExcludingEntity that checks a 3x3x3 cube of mini chunks (16x16x16 cubes within chunks) for entities.
	 * This is less efficient than the standard method, but necessary to fix a vanilla bug whereby repeated getEntity calls break if entities were moved between chunks in the same tick.
	 */
	private static void getEntitiesMultiChunk(AxisAlignedBB checkBox, World worldIn, ArrayList<Entity> entList){
		final double maxEntityRad = worldIn.getMaxEntityRadius();
		int i = MathHelper.floor((checkBox.minX - maxEntityRad) / 16.0D) - 1;
		int j = MathHelper.floor((checkBox.maxX + maxEntityRad) / 16.0D) + 1;
		int k = MathHelper.floor((checkBox.minZ - maxEntityRad) / 16.0D) - 1;
		int l = MathHelper.floor((checkBox.maxZ + maxEntityRad) / 16.0D) + 1;

		int yMin = MathHelper.clamp(MathHelper.floor((checkBox.minY - maxEntityRad) / 16.0D) - 1, 0, 15);
		int yMax = MathHelper.clamp(MathHelper.floor((checkBox.maxY + maxEntityRad) / 16.0D) + 1, 0, 15);

		for(int iLoop = i; iLoop <= j; iLoop++){
			for(int kLoop = k; kLoop <= l; kLoop++){
				if(worldIn.chunkExists(iLoop, kLoop)){
					Chunk chunk = worldIn.getChunkAt(new BlockPos(iLoop * 16, 100, kLoop * 16));
					for(int yLoop = yMin; yLoop <= yMax; yLoop++){
						if(!chunk.getEntityLists()[yLoop].isEmpty()){
							for(Entity entity : chunk.getEntityLists()[yLoop]){
								if(entity.getBoundingBox().intersects(checkBox)){
									entList.add(entity);
								}
							}
						}
					}
				}
			}
		}
	}
}