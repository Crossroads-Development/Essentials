package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@ObjectHolder(Essentials.MODID)
public class BrazierTileEntity extends BlockEntity implements ITickableTileEntity{

	@ObjectHolder("brazier")
	public static BlockEntityType<BrazierTileEntity> TYPE = null;

	public BrazierTileEntity(BlockPos pos, BlockState state){
		super(TYPE, pos, state);
	}

	@Override
	public void clientTick(){
		if(level.getGameTime() % 10 == 0){
			BlockState state = getBlockState();
			if(state.getBlock() != ESBlocks.brazier){
				setRemoved();
				return;
			}

			ClientLevel world = (ClientLevel) level;
			switch(state.getValue(ESProperties.BRAZIER_CONTENTS)){
				case 2:
					world.addAlwaysVisibleParticle(ParticleTypes.LAVA, worldPosition.getX() + .25 + (.5 * Math.random()), worldPosition.getY() + 1 + (Math.random() * .25D), worldPosition.getZ() + .25 + (.5 * Math.random()), 3, 0, 0.01D);
					break;
				case 3:
					world.addAlwaysVisibleParticle(ParticleTypes.FLAME, worldPosition.getX() + .25 + (.5 * Math.random()), worldPosition.getY() + 1 + (Math.random() * .25D), worldPosition.getZ() + .25 + (.5 * Math.random()), 3, 0, 0.01D);
					break;
				case 6:
					world.addAlwaysVisibleParticle(ParticleTypes.POOF, worldPosition.getX() + .25 + (.5 * Math.random()), worldPosition.getY() + 1 + (Math.random() * .25D), worldPosition.getZ() + .25 + (.5 * Math.random()), 0, -1, 0.01D);
					break;
			}
		}
	}

	public ItemStack useItem(ItemStack stack){
		int type = getBlockState().getValue(ESProperties.BRAZIER_CONTENTS);
		ItemStack out = stack;
		if(type == 0){
			//Put item in
			int tar = -1;
			if(stack.getItem() == Items.WATER_BUCKET){
				tar = 1;
				out = new ItemStack(Items.BUCKET);
			}else if(stack.getItem() == Items.LAVA_BUCKET){
				tar = 2;
				out = new ItemStack(Items.BUCKET);
			}else if((tar = getIdentifierForRawItem(stack)) >= 0){
				out = stack.copy();
				out.shrink(1);
			}

			if(tar >= 0){
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ESProperties.BRAZIER_CONTENTS, tar));
				return out;
			}
		}else{
			//Pull item out
			switch(type){
				case 1:
					if(stack.getItem() == Items.BUCKET && stack.getCount() == 1){
						level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ESProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Items.WATER_BUCKET);
					}
					break;
				case 2:
					if(stack.getItem() == Items.BUCKET && stack.getCount() == 1){
						level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ESProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Items.LAVA_BUCKET);
					}
					break;
				default:
					if(stack.isEmpty()){
						ItemStack removed = getRawItemFromIdentifier(type);
						if(!removed.isEmpty()){
							level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ESProperties.BRAZIER_CONTENTS, 0));
							return removed;
						}
					}
					break;
			}
		}

		return stack;
	}

	private int getIdentifierForRawItem(ItemStack stack){
		if(stack.getItem() == Blocks.COAL_BLOCK.asItem()){
			return 3;
		}else if(stack.getItem() == Blocks.GLOWSTONE.asItem()){
			return 4;
		}else if(stack.getItem() == Blocks.SOUL_SAND.asItem()){
			return 6;
		}
		return -1;
	}

	private ItemStack getRawItemFromIdentifier(int identifier){
		return switch(identifier){
			case 3 -> new ItemStack(Blocks.COAL_BLOCK);
			case 4 -> new ItemStack(Blocks.GLOWSTONE);
			case 6 -> new ItemStack(Blocks.SOUL_SAND);
			default -> ItemStack.EMPTY;
		};
	}

	private final FuelHandler fuelHandler = new FuelHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (LazyOptional<T>) LazyOptional.of(() -> fuelHandler);
		}
		return super.getCapability(cap, side);
	}

	private class FuelHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			if(slot != 0){
				return ItemStack.EMPTY;
			}
			return getRawItemFromIdentifier(getBlockState().getValue(ESProperties.BRAZIER_CONTENTS));
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot == 0 && getBlockState().getValue(ESProperties.BRAZIER_CONTENTS) == 0){
				int tar = getIdentifierForRawItem(stack);

				if(tar >= 0){
					if(!simulate){
						level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(ESProperties.BRAZIER_CONTENTS, tar));
					}
					ItemStack out = stack.copy();
					out.shrink(1);
					return out;
				}
			}
			return stack;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			if(slot == 0){
				ItemStack  out = getStackInSlot(0);
				if(!simulate){
					level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ESProperties.BRAZIER_CONTENTS, 0));
				}
				return out;
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return slot == 0 ? 1 : 0;
		}

		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack){
			return slot == 0 && getIdentifierForRawItem(stack) >= 0;
		}
	}
}
