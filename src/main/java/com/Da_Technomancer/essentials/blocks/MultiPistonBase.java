package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.WorldBuffer;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Notable differences from a normal piston include:
 * 15 block head range, distance controlled by signal strength,
 * No quasi-connectivity,
 * Redstone can be placed on top of the piston,
 * Hit box does not change when extended,
 * Piston extension and retraction is instant, no 2-tick delay or rendering of block movement.
 * Can move up to 64 blocks at a time instead of 12
 */
public class MultiPistonBase extends Block{

	private final boolean sticky;

	protected MultiPistonBase(boolean sticky){
		super(Material.PISTON);
		String name = "multi_piston" + (sticky ? "_sticky" : "");
		setTranslationKey(name);
		setRegistryName(name);
		this.sticky = sticky;
		setHardness(0.5F);
		setCreativeTab(EssentialsItems.TAB_ESSENTIALS);
		setDefaultState(getDefaultState().withProperty(EssentialsProperties.FACING, EnumFacing.NORTH).withProperty(EssentialsProperties.REDSTONE_BOOL, false));
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		return getDefaultState().withProperty(EssentialsProperties.FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(EssentialsConfig.isWrench(playerIn.getHeldItem(hand), worldIn.isRemote) && getExtension(worldIn, pos, state.getValue(EssentialsProperties.FACING)) == 0){
			if(!worldIn.isRemote){
				IBlockState endState = state.cycleProperty(EssentialsProperties.FACING);
				worldIn.setBlockState(pos, endState);
				checkRedstone(worldIn, pos, endState.getValue(EssentialsProperties.FACING));
			}
			return true;
		}
		return false;
	}

	protected void safeBreak(World worldIn, BlockPos pos){
		if(safeToBreak){
			worldIn.destroyBlock(pos, true);
		}
	}

	private boolean safeToBreak = true;

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand){
		checkRedstone(worldIn, pos, state.getValue(EssentialsProperties.FACING));
	}

	private void checkRedstone(World worldIn, BlockPos pos, EnumFacing dir){
		int i = Math.max(worldIn.getRedstonePower(pos.down(), EnumFacing.DOWN), Math.max(worldIn.getRedstonePower(pos.up(), EnumFacing.UP), Math.max(worldIn.getRedstonePower(pos.east(), EnumFacing.EAST), Math.max(worldIn.getRedstonePower(pos.west(), EnumFacing.WEST), Math.max(worldIn.getRedstonePower(pos.north(), EnumFacing.NORTH), worldIn.getRedstonePower(pos.south(), EnumFacing.SOUTH))))));
		if(i > 0){
			if(!worldIn.getBlockState(pos).getValue(EssentialsProperties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(EssentialsProperties.REDSTONE_BOOL, true));
			}
		}else{
			if(worldIn.getBlockState(pos).getValue(EssentialsProperties.REDSTONE_BOOL)){
				worldIn.setBlockState(pos, worldIn.getBlockState(pos).withProperty(EssentialsProperties.REDSTONE_BOOL, false));
			}
		}

		int prev = getExtension(worldIn, pos, dir);
		if(prev != i && prev != -1){
			safeToBreak = false;
			setExtension(worldIn, pos, dir, i, prev);
			safeToBreak = true;
			if(!worldIn.isBlockTickPending(pos, this)){
				worldIn.updateBlockTick(pos, this, 1, -1);
			}
		}
	}

	private int getExtension(World worldIn, BlockPos pos, EnumFacing dir){
		if(!safeToBreak){
			return -1;
		}
		final Block GOAL = sticky ? EssentialsBlocks.multiPistonExtendSticky : EssentialsBlocks.multiPistonExtend;
		for(int i = 1; i <= 15; i++){
			if(worldIn.getBlockState(pos.offset(dir, i)).getBlock() != GOAL || worldIn.getBlockState(pos.offset(dir, i)).getValue(EssentialsProperties.FACING) != dir){
				return i - 1;
			}
		}
		return 15;
	}

	private void setExtension(World worldIn, BlockPos pos, EnumFacing dir, int distance, int prev){
		if(prev == distance){
			return;
		}

		final WorldBuffer world = new WorldBuffer(worldIn);
		final Block GOAL = sticky ? EssentialsBlocks.multiPistonExtendSticky : EssentialsBlocks.multiPistonExtend;
		for(int i = 1; i <= prev; i++){
			if(world.getBlockState(pos.offset(dir, i)).getBlock() == GOAL && world.getBlockState(pos.offset(dir, i)).getValue(EssentialsProperties.FACING) == dir){
				world.addChange(pos.offset(dir, i), Blocks.AIR.getDefaultState());
			}
		}

		if(sticky && prev > distance){
			for(int i = prev + 1; i > distance + 1; i--){
				ArrayList<BlockPos> list = new ArrayList<>();

				if(canPush(world.getBlockState(pos.offset(dir, i)), false)){
					if(propogate(list, world, pos.offset(dir, i), dir.getOpposite(), null)){
						break;
					}else{
						for(int index = list.size() - 1; index >= 0; --index){
							BlockPos moving = list.get(index);

							if(world.getBlockState(moving.offset(dir.getOpposite())).getPushReaction() == EnumPushReaction.DESTROY){
								world.getBlockState(moving.offset(dir.getOpposite())).getBlock().dropBlockAsItem(worldIn, moving.offset(dir.getOpposite()), world.getBlockState(moving.offset(dir.getOpposite())), 0);
							}
							world.addChange(moving.offset(dir.getOpposite()), world.getBlockState(moving));
							world.addChange(moving, Blocks.AIR.getDefaultState());
						}
					}
				}
			}
		}

		if(distance == 0){
			if(world.hasChanges()){
				worldIn.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.BLOCKS, .5F, (worldIn.rand.nextFloat() * .15F) + .6F);
			}
			world.doChanges();
			return;
		}

		for(int i = 1; i <= distance; i++){
			ArrayList<BlockPos> list = new ArrayList<>();

			if(canPush(world.getBlockState(pos.offset(dir, i)), false)){
				if(propogate(list, world, pos.offset(dir, i), dir, null)){
					if(world.hasChanges()){
						worldIn.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, .5F, (worldIn.rand.nextFloat() * .25F) + .6F);
					}
					world.doChanges();
					return;
				}
			}else if(!canPush(world.getBlockState(pos.offset(dir, i)), true)){
				if(world.hasChanges()){
					worldIn.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, .5F, (worldIn.rand.nextFloat() * .25F) + .6F);
				}
				world.doChanges();
				return;
			}

			if(list.isEmpty()){
				for(Entity ent : getEntitiesMultiChunk(FULL_BLOCK_AABB.offset(pos.offset(dir, i)), worldIn)){
					if(ent.getPushReaction() != EnumPushReaction.IGNORE){
						ent.setPositionAndUpdate(ent.posX + (double) dir.getXOffset(), ent.posY + (double) dir.getYOffset(), ent.posZ + (double) dir.getZOffset());
					}
				}
			}else{
				for(int index = list.size() - 1; index >= 0; --index){
					BlockPos moving = list.get(index);

					if(world.getBlockState(moving.offset(dir)).getPushReaction() == EnumPushReaction.DESTROY){
						worldIn.destroyBlock(moving.offset(dir), true);
					}
					world.addChange(moving.offset(dir), world.getBlockState(moving));
					world.addChange(moving, Blocks.AIR.getDefaultState());
					AxisAlignedBB box;
					//Due to the fact that the block isn't actually at that position (WorldBuffer), exceptions have to be caught.
					try{
						box = world.getBlockState(moving.offset(dir)).getCollisionBoundingBox(worldIn, pos);
					}catch(Exception e){
						box = FULL_BLOCK_AABB;
					}
					box = box.offset(moving.offset(dir));
					for(Entity ent : getEntitiesMultiChunk(box, worldIn)){
						if(ent.getPushReaction() != EnumPushReaction.IGNORE){
							ent.setPositionAndUpdate(ent.posX + (double) dir.getXOffset(), ent.posY + (double) dir.getYOffset(), ent.posZ + (double) dir.getZOffset());
							if(world.getBlockState(moving.offset(dir)).getBlock() == Blocks.SLIME_BLOCK){
								ent.addVelocity(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
								ent.velocityChanged = true;
							}
						}
					}
				}
			}

			for(int j = i; j >= 1; j--){
				if(world.getBlockState(pos.offset(dir, j)).getPushReaction() == EnumPushReaction.DESTROY){
					worldIn.destroyBlock(pos.offset(dir, j), true);
				}
				world.addChange(pos.offset(dir, j), GOAL.getDefaultState().withProperty(EssentialsProperties.FACING, dir).withProperty(EssentialsProperties.HEAD, i == j));
			}
		}
		if(world.hasChanges()){
			worldIn.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, .5F, (worldIn.rand.nextFloat() * .25F) + .6F);
		}
		world.doChanges();
	}

	private static boolean canPush(IBlockState state, boolean blocking){
		if(blocking){
			return (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON) ? !state.getValue(BlockPistonBase.EXTENDED) : state.getPushReaction() != EnumPushReaction.BLOCK && !state.getBlock().hasTileEntity(state) && state.getBlock() != Blocks.OBSIDIAN && state.getBlockHardness(null, null) >= 0;
		}else{
			return (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON) ? !state.getValue(BlockPistonBase.EXTENDED) : state.getPushReaction() == EnumPushReaction.NORMAL && state.getMaterial() != Material.AIR && !state.getBlock().hasTileEntity(state) && state.getBlock() != Blocks.OBSIDIAN && state.getBlockHardness(null, null) >= 0;
		}
	}

	private static final int PUSH_LIMIT = 64;

	/**
	 * Used recursively to fill a list with the blocks to be moved. Returns true if there is a problem that stops the movement.
	 */
	private static boolean propogate(ArrayList<BlockPos> list, WorldBuffer buf, BlockPos pos, EnumFacing dir, @Nullable BlockPos forward){
		if(list.contains(pos)){
			return false;
		}
		if(!canPush(buf.getBlockState(pos.offset(dir)), true)){
			return true;
		}
		if(forward == null){
			list.add(pos);
		}else{
			list.add(list.indexOf(forward), pos);
		}

		if(buf.getBlockState(pos).getBlock() == Blocks.SLIME_BLOCK){
			//The back has to be checked before the sides or the list ordering gets messed up.
			//Likewise, the sides have to be sent before the front
			if(canPush(buf.getBlockState(pos.offset(dir.getOpposite())), false)){
				if(list.size() > PUSH_LIMIT || propogate(list, buf, pos.offset(dir.getOpposite()), dir, pos)){
					return true;
				}
			}

			for(EnumFacing checkDir : EnumFacing.VALUES){
				if(checkDir != dir && checkDir != dir.getOpposite()){
					if(canPush(buf.getBlockState(pos.offset(checkDir)), false)){
						if(list.size() > PUSH_LIMIT || propogate(list, buf, pos.offset(checkDir), dir, pos)){
							return true;
						}
					}
				}
			}
		}

		return canPush(buf.getBlockState(pos.offset(dir)), false) && (list.size() > PUSH_LIMIT || propogate(list, buf, pos.offset(dir), dir, null));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state){
		setExtension(world, pos, state.getValue(EssentialsProperties.FACING), 0, getExtension(world, pos, state.getValue(EssentialsProperties.FACING)));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
		neighborChanged(world.getBlockState(pos), world, pos, null, null);
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos){
		if(worldIn.isRemote){
			return;
		}
		checkRedstone(worldIn, pos, state.getValue(EssentialsProperties.FACING));
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, EssentialsProperties.FACING, EssentialsProperties.REDSTONE_BOOL);
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(EssentialsProperties.FACING, EnumFacing.byIndex(meta & 7)).withProperty(EssentialsProperties.REDSTONE_BOOL, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(EssentialsProperties.FACING).getIndex() + (state.getValue(EssentialsProperties.REDSTONE_BOOL) ? 8 : 0);
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state){
		return state.getValue(EssentialsProperties.REDSTONE_BOOL) ? EnumPushReaction.BLOCK : EnumPushReaction.NORMAL;
	}


	/**
	 * An alternate version of World#getEntitiesWithinAABBExcludingEntity that checks a 3x3x3 cube of mini chunks (16x16x16 cubes within chunks) for entities.
	 * This is less efficient than the standard method, but necessary to fix a bug.
	 */
	private static ArrayList<Entity> getEntitiesMultiChunk(AxisAlignedBB checkBox, World worldIn){
		ArrayList<Entity> found = new ArrayList<Entity>();

		int i = MathHelper.floor((checkBox.minX - World.MAX_ENTITY_RADIUS) / 16.0D) - 1;
		int j = MathHelper.floor((checkBox.maxX + World.MAX_ENTITY_RADIUS) / 16.0D) + 1;
		int k = MathHelper.floor((checkBox.minZ - World.MAX_ENTITY_RADIUS) / 16.0D) - 1;
		int l = MathHelper.floor((checkBox.maxZ + World.MAX_ENTITY_RADIUS) / 16.0D) + 1;

		int yMin = MathHelper.clamp(MathHelper.floor((checkBox.minY - World.MAX_ENTITY_RADIUS) / 16.0D) - 1, 0, 15);
		int yMax = MathHelper.clamp(MathHelper.floor((checkBox.maxY + World.MAX_ENTITY_RADIUS) / 16.0D) + 1, 0, 15);

		for(int iLoop = i; iLoop <= j; iLoop++){
			for(int kLoop = k; kLoop <= l; kLoop++){
				if(((ChunkProviderServer) worldIn.getChunkProvider()).chunkExists(iLoop, kLoop)){
					Chunk chunk = worldIn.getChunk(iLoop, kLoop);
					for(int yLoop = yMin; yLoop <= yMax; ++yLoop){
						if(!chunk.getEntityLists()[yLoop].isEmpty()){
							for(Entity entity : chunk.getEntityLists()[yLoop]){
								if(entity.getEntityBoundingBox().intersects(checkBox)){
									found.add(entity);
								}
							}
						}
					}
				}
			}
		}

		return found;
	}

	private static final AxisAlignedBB[] BB = new AxisAlignedBB[] {new AxisAlignedBB(0, 5D / 16D, 0, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 1, 11D / 16D, 1), new AxisAlignedBB(0, 0, 5D / 16D, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 1, 1, 11D / 16D), new AxisAlignedBB(5D / 16D, 0, 0, 1, 1, 1), new AxisAlignedBB(0, 0, 0, 11D / 16D, 1, 1)};

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean pleaseDontBeRelevantToAnythingOrIWillBeSad){
		if(state.getValue(EssentialsProperties.REDSTONE_BOOL)){
			addCollisionBoxToList(pos, mask, list, BB[state.getValue(EssentialsProperties.FACING).getIndex()]);
		}else{
			addCollisionBoxToList(pos, mask, list, FULL_BLOCK_AABB);
		}
	}
}