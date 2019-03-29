package com.Da_Technomancer.essentials.tileentities;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.Particles;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@ObjectHolder(Essentials.MODID)
public class BrazierTileEntity extends TileEntity implements ITickable{

	@ObjectHolder("brazier")
	private static TileEntityType<BrazierTileEntity> TYPE = null;

	public BrazierTileEntity(){
		super(TYPE);
	}

	@Override
	public void tick(){
		if(world.isRemote){
			return;
		}

		if(world.getGameTime() % 10 == 0){
			WorldServer server = (WorldServer) world;

			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() != EssentialsBlocks.brazier){
				remove();
				return;
			}
			switch(world.getBlockState(pos).get(EssentialsProperties.BRAZIER_CONTENTS)){
				case 2:
					server.spawnParticle(Particles.LAVA, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 3, 0, 0, 0, 0.01D);
					break;
				case 3:
					server.spawnParticle(Particles.FLAME, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 3, 0, 0, 0, 0.01D);
					break;
				case 6:
					server.spawnParticle(Particles.POOF, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 0, -1, 0.5D, 1, 0.01D);
					break;
				case 7:
					server.spawnParticle(Particles.AMBIENT_ENTITY_EFFECT, pos.getX() + .25 + (.5 * Math.random()), pos.getY() + 1 + (Math.random() * .25D), pos.getZ() + .25 + (.5 * Math.random()), 1, 0, 0, 0, 0.01D);
					ItemStack out = recipeMatch((ArrayList<EntityItem>) world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(1, 1, 1)), EntitySelectors.IS_ALIVE));
					if(!out.isEmpty()){
						for(EntityItem item : world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(1, 1, 1)), EntitySelectors.IS_ALIVE)){
							item.remove();
						}

						server.createExplosion(null, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, 0, false);
						EntityItem item = new EntityItem(world, pos.getX(), pos.getY() + 1, pos.getZ(), out.copy());
						item.setInvulnerable(true);
						world.spawnEntity(item);
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
					}
					break;
			}
		}
	}

	public ItemStack useItem(ItemStack stack){
		int type = world.getBlockState(pos).get(EssentialsProperties.BRAZIER_CONTENTS);
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
			}else if(stack.getItem() == Items.WATER_BUCKET){
				tar = 1;
				out = new ItemStack(Items.BUCKET);
			}else if(stack.getItem() == Items.LAVA_BUCKET){
				tar = 2;
				out = new ItemStack(Items.BUCKET);
			}



			/*
			LazyOptional<IFluidHandlerItem> handlerCont;
			if(tar == 0 && (handlerCont = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)).isPresent()){
				IFluidHandlerItem handler = handlerCont.orElseThrow(NullPointerException::new);
				FluidStack drained = handler.drain(new FluidStack(Fluids.WATER, 1000), false);
				if(drained != null && drained.amount == 1000){
					handler.drain(new FluidStack(FluidRegistry.WATER, 1000), true);
					world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 1));
					return handler.getContainer();
				}else{
					drained = handler.drain(new FluidStack(FluidRegistry.LAVA, 1000), false);
					if(drained != null && drained.amount == 1000){
						handler.drain(new FluidStack(FluidRegistry.LAVA, 1000), true);
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 2));
						return handler.getContainer();
					}
				}
			}
			*/

			if(tar != 0){
				world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, tar));
				return out;
			}
		}else{
			switch(type){
				case 1:
					if(stack.getItem() == Items.BUCKET && stack.getCount() == 1){
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Items.WATER_BUCKET);
					}
					/*

					if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)){
						IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						if(handler.fill(new FluidStack(FluidRegistry.WATER, 1000), false) == 1000){
							handler.fill(new FluidStack(FluidRegistry.WATER, 1000), true);
							world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
							return handler.getContainer();
						}
					}
					*/
					break;
				case 2:
					if(stack.getItem() == Items.BUCKET && stack.getCount() == 1){
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Items.LAVA_BUCKET);
					}
					/*
					if(stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)){
						IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
						if(handler.fill(new FluidStack(FluidRegistry.LAVA, 1000), false) == 1000){
							handler.fill(new FluidStack(FluidRegistry.LAVA, 1000), true);
							world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
							return handler.getContainer();
						}
					}
					*/
					break;
				case 3:
					if(stack.isEmpty()){
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Blocks.COAL_BLOCK);
					}
					break;
				case 4:
					if(stack.isEmpty()){
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Blocks.GLOWSTONE);
					}
					break;
				case 6:
					if(stack.isEmpty()){
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
						return new ItemStack(Blocks.SOUL_SAND);
					}
					break;
				case 7:
					if(stack.isEmpty()){
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, EnumFacing side){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
			return LazyOptional.of((NonNullSupplier) () -> fuelHandler);
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
			switch(world.getBlockState(pos).get(EssentialsProperties.BRAZIER_CONTENTS)){
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
			if(slot == 0 && world.getBlockState(pos).get(EssentialsProperties.BRAZIER_CONTENTS) == 0){
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
						world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, tar));
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
					world.setBlockState(pos, world.getBlockState(pos).with(EssentialsProperties.BRAZIER_CONTENTS, 0));
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
			return slot == 0 && (stack.getItem() == Item.getItemFromBlock(Blocks.COAL_BLOCK) || stack.getItem() == Item.getItemFromBlock(Blocks.GLOWSTONE) || stack.getItem() == Items.POISONOUS_POTATO || stack.getItem() == Item.getItemFromBlock(Blocks.SOUL_SAND));
		}
	}


	@Nonnull
	private static ItemStack recipeMatch(ArrayList<EntityItem> itemEnt){
		if(itemEnt == null){
			return ItemStack.EMPTY;
		}

		ArrayList<ItemStack> items = new ArrayList<>();

		for(EntityItem it : itemEnt){
			if(it.getItem().isEmpty() || it.getItem().getCount() != 1){
				return ItemStack.EMPTY;
			}
			items.add(it.getItem());
		}

		if(items.size() != 3){
			return ItemStack.EMPTY;
		}

		/*
		for(Pair<Predicate<ItemStack>[], ItemStack> craft : EssentialsCrafting.brazierBoboRecipes){
			ArrayList<ItemStack> itemCop = new ArrayList<>(items);

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
		*/

		return ItemStack.EMPTY;
	}
}
