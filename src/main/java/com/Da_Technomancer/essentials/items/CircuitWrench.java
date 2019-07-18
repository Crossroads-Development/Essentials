package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractCircuit;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractTile;
import com.Da_Technomancer.essentials.gui.container.CircuitWrenchContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CircuitWrench extends Item{

	/**
	 * Public for read-only; Modify using registerCircuit()
	 */
	public static final ArrayList<AbstractTile> MODES = new ArrayList<>(7);
	/**
	 * Public for read-only; Modify using registerCircuit()
	 */
	public static final ArrayList<ResourceLocation> ICONS = new ArrayList<>(7);

	public static final String NBT_KEY = Essentials.MODID + ":mode";
	private static final Tag<Item> COMPONENT_TAG = new ItemTags.Wrapper(new ResourceLocation(Essentials.MODID, "circuit_components"));//new ResourceLocation(Essentials.MODID, "circuit_components");

	static{
		registerCircuit(EssentialsBlocks.wireCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire.png"));
		registerCircuit(EssentialsBlocks.wireJunctionCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire_junction.png"));
		registerCircuit(EssentialsBlocks.interfaceCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/interface.png"));
		registerCircuit(EssentialsBlocks.consCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/constant.png"));
		registerCircuit(EssentialsBlocks.notCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/not.png"));
		registerCircuit(EssentialsBlocks.andCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/and.png"));
		registerCircuit(EssentialsBlocks.xorCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/xor.png"));
	}

	/**
	 * This is a public method addons can use to add their circuits to the CircuitWrench
	 * @param toRegister The circuit to be registered
	 * @param icon A path to a valid square icon to represent the circuit. If null, uses the generic missing texture
	 */
	@SuppressWarnings("all")//It doesn't like the narrowly used public method
	public static void registerCircuit(@Nonnull AbstractTile toRegister, @Nullable ResourceLocation icon){
		if(!MODES.contains(toRegister)){
			MODES.add(toRegister);
			ICONS.add(icon);
		}else{
			Essentials.logger.info("Redundant circuit registration: " + toRegister.getRegistryName());
		}
	}

	protected CircuitWrench(){
		super(new Item.Properties().maxStackSize(1).group(EssentialsItems.TAB_ESSENTIALS));
		String name = "circuit_wrench";
		setRegistryName(name);
		EssentialsItems.toRegister.add(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		ItemStack stack = playerIn.getHeldItem(handIn);
		if(playerIn.isSneaking()){
			if(!worldIn.isRemote){
				NetworkHooks.openGui((ServerPlayerEntity) playerIn, UIProvider.INSTANCE);
			}
			return ActionResult.newResult(ActionResultType.SUCCESS, stack);
		}

		return ActionResult.newResult(ActionResultType.SUCCESS, stack);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		BlockState state = context.getWorld().getBlockState(context.getPos());
		BlockState toPlace = MODES.get(context.getItem().getOrCreateTag().getInt(NBT_KEY) % MODES.size()).getDefaultState();

		if(!context.getPlayer().isSneaking() && state.getBlock() instanceof AbstractTile){
			if(state.getBlock() == toPlace.getBlock()){
				return ActionResultType.SUCCESS;
			}


			boolean allowed = false;
			if(context.getPlayer().isCreative()){
				//Creative mode is free
				allowed = true;
			}else if(toPlace.getBlock() instanceof AbstractCircuit){
				if(state.getBlock() instanceof AbstractCircuit){
					//Circuit->circuit is free
					allowed = true;
				}else{
					//Have to pay for tile->circuit
					List<ItemStack> playerInv = context.getPlayer().inventory.mainInventory;
					for(ItemStack stack : playerInv){
						if(COMPONENT_TAG.contains(stack.getItem())){
							if(!context.getWorld().isRemote){
								stack.shrink(1);
							}
							allowed = true;
							break;
						}
					}
				}
			}else{
				//Non-circuits are free
				allowed = true;

				if(state.getBlock() instanceof AbstractCircuit){
					//If we downgrade from a circuit to a non-circuit tile (like wire or junction), return a circuit component
					ItemStack given = new ItemStack(COMPONENT_TAG.getRandomElement(context.getWorld().rand), 1);
					if(!given.isEmpty()){
						context.getPlayer().addItemStackToInventory(given);
					}
				}
			}

			if(allowed){
				if(toPlace.has(EssentialsProperties.HORIZ_FACING)){
					if(state.has(EssentialsProperties.HORIZ_FACING)){
						toPlace = toPlace.with(EssentialsProperties.HORIZ_FACING, state.get(EssentialsProperties.HORIZ_FACING));
					}else{
						toPlace = toPlace.with(EssentialsProperties.HORIZ_FACING, context.getPlayer().getAdjustedHorizontalFacing());
					}
				}
				context.getWorld().setBlockState(context.getPos(), toPlace);
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	private static final Style style = new Style().setColor(TextFormatting.DARK_RED);

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		int mode = stack.getOrCreateTag().getInt(NBT_KEY) % MODES.size();
		tooltip.add(new TranslationTextComponent("tt.essentials.circuit_wrench_setting").setStyle(style).appendSibling(new TranslationTextComponent(MODES.get(mode).getTranslationKey())));
		tooltip.add(new TranslationTextComponent("tt.essentials.circuit_wrench_info"));
		tooltip.add(new TranslationTextComponent("tt.essentials.circuit_wrench_change_mode"));
	}

	private static class UIProvider implements INamedContainerProvider{

		private static final UIProvider INSTANCE = new UIProvider();

		@Nullable
		@Override
		public Container createMenu(int menuId, PlayerInventory playerInv, PlayerEntity player){
			return new CircuitWrenchContainer(menuId, playerInv, null);
		}

		@Override
		public ITextComponent getDisplayName(){
			return new TranslationTextComponent("container.circuit_wrench");
		}
	}
}
