package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.items.crafting.EssentialsCrafting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Predicate;

public class BrazierTileEntity extends TileEntity implements ITickable{

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
		return (oldState.getBlock() != newState.getBlock());
	}

	@Override
	public void update(){
		if(world.isRemote){
			return;
		}

		if(world.getTotalWorldTime() % 10 == 0){
			WorldServer server = (WorldServer) world;

			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() != EssentialsBlocks.brazier){
				invalidate();
				return;
			}
			switch(world.getBlockState(pos).getValue(EssentialsProperties.BRAZIER_CONTENTS)){
				case 2:
					server.spawnParticle(EnumParticleTypes.LAVA, false, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 3, 0, 0, 0, 0.01D);
					break;
				case 3:
					server.spawnParticle(EnumParticleTypes.FLAME, false, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 3, 0, 0, 0, 0.01D);
					break;
				case 6:
					server.spawnParticle(EnumParticleTypes.REDSTONE, false, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 0, -1, 0.5D, 1, 0.01D);
					break;
				case 7:
					server.spawnParticle(EnumParticleTypes.SPELL_MOB, false, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 1, 0, 0, 0, 0.01D);
					ItemStack out = recipeMatch((ArrayList<EntityItem>) world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(1, 1, 1)), EntitySelectors.IS_ALIVE));
					if(!out.isEmpty()){
						for(EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(1, 1, 1)), EntitySelectors.IS_ALIVE)){
							item.setDead();
						}

						server.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, 0, false);
						EntityItem item = new EntityItem(world, pos.getX(), pos.getY() + 1, pos.getZ(), out.copy());
						item.setEntityInvulnerable(true);
						world.spawnEntity(item);
						world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
					}
					break;
			}
		}
	}

	public ItemStack useItem(ItemStack stack){
		int type = world.getBlockState(pos).getValue(EssentialsProperties.BRAZIER_CONTENTS);
		ItemStack out = stack;
		if(type == 0){
			int tar = 0;
			if(stack.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK)){
				tar = 3;
				out = stack.copy();
				out.shrink(1);
			}else if(stack.getItem() == Item.getItemFromBlock(Blocks.GLOWSTONE)){
				tar = 4;
				out = stack.copy();
				out.shrink(1);
			}else if(stack.getItem() == Items.POISONOUS_POTATO){
				tar = 7;
				out = stack.copy();
				out.shrink(1);
			}else if(stack.getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND)){
				tar = 6;
				out = stack.copy();
				out.shrink(1);
			}
			if(tar == 0 && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)){
				IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
				FluidStack drained = handler.drain(new FluidStack(FluidRegistry.WATER, 1000), false);
				if(drained != null && drained.amount == 1000){
					handler.drain(new FluidStack(FluidRegistry.WATER, 1000), true);
					world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 1));
					return handler.getContainer();
				}else{
					drained = handler.drain(new FluidStack(FluidRegistry.LAVA, 1000), false);
					if(drained != null && drained.amount == 1000){
						handler.drain(new FluidStack(FluidRegistry.LAVA, 1000), true);
						world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 2));
						return handler.getContainer();
					}
				}
			}

			if(tar != 0){
				world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, tar));
				return out;
			}
		}else{
			switch(type){
				case 1:
					if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)){
						IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						if(handler.fill(new FluidStack(FluidRegistry.WATER, 1000), false) == 1000){
							handler.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
							world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
							return handler.getContainer();
						}
					}
					break;
				case 2:
					if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)){
						IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						if(handler.fill(new FluidStack(FluidRegistry.LAVA, 1000), false) == 1000){
							handler.fill(new FluidStack(FluidRegistry.LAVA, 1000), true);
							world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
							return handler.getContainer();
						}
					}
					break;
				case 3:
					if(stack.isEmpty()){
						world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Blocks.COAL_BLOCK);
					}
					break;
				case 4:
					if(stack.isEmpty()){
						world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Blocks.GLOWSTONE);
					}
					break;
				case 6:
					if(stack.isEmpty()){
							world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
							return new ItemStack(Blocks.SOUL_SAND);
					}
					break;
				case 7:
					if(stack.isEmpty()){
						world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Items.POISONOUS_POTATO);
					}
					break;
				default:
					return ItemStack.EMPTY;
			}
		}


		return stack;
	}

	private final FuelHandler fuelHandler = new FuelHandler();

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return true;
		}
		return super.hasCapability(cap, side);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return (T) fuelHandler;
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
			switch(world.getBlockState(pos).getValue(EssentialsProperties.BRAZIER_CONTENTS)){
				case 3:
					return new ItemStack(Blocks.COAL_BLOCK);
				case 4:
					return new ItemStack(Blocks.GLOWSTONE);
				case 6:
					return new ItemStack(Blocks.SOUL_SAND);
				case 7:
					return new ItemStack(Items.POISONOUS_POTATO);
				default:
					return ItemStack.EMPTY;
			}
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot == 0 && world.getBlockState(pos).getValue(EssentialsProperties.BRAZIER_CONTENTS) == 0){
				int tar = 0;
				if(stack.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK)){
					tar = 3;
				}else if(stack.getItem() == Item.getItemFromBlock(Blocks.GLOWSTONE)){
					tar = 4;
				}else if(stack.getItem() == Items.POISONOUS_POTATO){
					tar = 7;
				}else if(stack.getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND)){
					tar = 6;
				}

				if(tar != 0){
					if(!simulate){
						world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, tar));
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
					world.setBlockState(pos, world.getBlockState(pos).withProperty(EssentialsProperties.BRAZIER_CONTENTS, 0));
				}
				return out;
			}
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot){
			return slot == 0 ? 1 : 0;
		}
	}


	@Nonnull
	private static ItemStack recipeMatch(ArrayList<EntityItem> itemEnt){
		if(itemEnt == null){
			return ItemStack.EMPTY;
		}

		ArrayList<ItemStack> items = new ArrayList<ItemStack>();

		for(EntityItem it : itemEnt){
			if(it.getItem().isEmpty() || it.getItem().getCount() != 1){
				return ItemStack.EMPTY;
			}
			items.add(it.getItem());
		}

		if(items.size() != 3){
			return ItemStack.EMPTY;
		}

		for(Pair<Predicate<ItemStack>[], ItemStack> craft : EssentialsCrafting.brazierBoboRecipes){
			ArrayList<ItemStack> itemCop = new ArrayList<ItemStack>(items);

			for(Predicate<ItemStack> cStack : craft.getLeft()){
				for(ItemStack stack : items){
					if(itemCop.contains(stack) && cStack.test(stack)){
						itemCop.remove(stack);
						break;
					}
				}

				if(itemCop.size() == 0){
					return craft.getRight();
				}
			}
		}

		return ItemStack.EMPTY;
	}
}
