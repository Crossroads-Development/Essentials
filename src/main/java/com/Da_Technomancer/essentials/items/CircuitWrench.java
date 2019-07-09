package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.blocks.EssentialsProperties;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractCircuit;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CircuitWrench extends Item{

	private static final ArrayList<AbstractTile> MODES = new ArrayList<>(7);
	private static final ArrayList<ResourceLocation> ICONS = new ArrayList<>(7);

	private static final String NBT_KEY = Essentials.MODID + ":mode";
	private static final Tag<Item> COMPONENT_TAG = new ItemTags.Wrapper(new ResourceLocation(Essentials.MODID, "circuit_components"));//new ResourceLocation(Essentials.MODID, "circuit_components");

	static{
		registerCircuit(EssentialsBlocks.wireCircuit, null);
		registerCircuit(EssentialsBlocks.wireJunctionCircuit, null);
		registerCircuit(EssentialsBlocks.interfaceCircuit, null);
		registerCircuit(EssentialsBlocks.consCircuit, null);
		registerCircuit(EssentialsBlocks.notCircuit, null);
		registerCircuit(EssentialsBlocks.andCircuit, null);
		registerCircuit(EssentialsBlocks.xorCircuit, null);
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
			int newMode = (stack.getTag().getInt(NBT_KEY) + 1) % MODES.size();
			stack.getOrCreateTag().putInt(NBT_KEY, newMode);
			if(worldIn.isRemote){
				playerIn.sendMessage(new TranslationTextComponent("tt.essentials.circuit_wrench_setting").setStyle(style).appendSibling(new TranslationTextComponent(MODES.get(newMode).getTranslationKey())));
			}
			//TODO UI
			return ActionResult.newResult(ActionResultType.SUCCESS, stack);
		}

		return ActionResult.newResult(ActionResultType.SUCCESS, stack);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		for(ResourceLocation loc : Items.QUARTZ.getTags()){
			System.out.println(loc.toString());
		}

		BlockState state = context.getWorld().getBlockState(context.getPos());
		BlockState toPlace = MODES.get(context.getItem().getOrCreateTag().getInt(NBT_KEY) % MODES.size()).getDefaultState();

		if(state.getBlock() instanceof AbstractTile){
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
}
