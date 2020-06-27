package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
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
import net.minecraft.tags.ITag;
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
	public static final ArrayList<AbstractTile> MODES = new ArrayList<>(32);
	/**
	 * Public for read-only; Modify using registerCircuit()
	 */
	public static final ArrayList<ResourceLocation> ICONS = new ArrayList<>(32);

	public static final String NBT_KEY = Essentials.MODID + ":mode";
	private static final ITag<Item> COMPONENT_TAG = new ItemTags.Wrapper(new ResourceLocation(Essentials.MODID, "circuit_components"));//new ResourceLocation(Essentials.MODID, "circuit_components");

	static{
		registerCircuit(ESBlocks.wireCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire.png"));
		registerCircuit(ESBlocks.wireJunctionCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire_junction.png"));
		registerCircuit(ESBlocks.interfaceCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/interface.png"));
		registerCircuit(ESBlocks.consCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/constant.png"));
		registerCircuit(ESBlocks.notCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/not.png"));
		registerCircuit(ESBlocks.andCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/and.png"));
		registerCircuit(ESBlocks.orCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/or.png"));
		registerCircuit(ESBlocks.xorCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/xor.png"));
		registerCircuit(ESBlocks.readerCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/reader.png"));
		registerCircuit(ESBlocks.sumCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/sum.png"));
		registerCircuit(ESBlocks.difCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/dif.png"));
		registerCircuit(ESBlocks.prodCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/prod.png"));
		registerCircuit(ESBlocks.quotCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/quot.png"));
		registerCircuit(ESBlocks.invCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/inv.png"));
		registerCircuit(ESBlocks.moduloCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/modulo.png"));
		registerCircuit(ESBlocks.powCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pow.png"));
		registerCircuit(ESBlocks.logCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/log.png"));
		registerCircuit(ESBlocks.sinCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/sin.png"));
		registerCircuit(ESBlocks.cosCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/cos.png"));
		registerCircuit(ESBlocks.tanCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/tan.png"));
		registerCircuit(ESBlocks.asinCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/asin.png"));
		registerCircuit(ESBlocks.acosCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/acos.png"));
		registerCircuit(ESBlocks.atanCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/atan.png"));
		registerCircuit(ESBlocks.maxCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/max.png"));
		registerCircuit(ESBlocks.minCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/min.png"));
		registerCircuit(ESBlocks.roundCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/round.png"));
		registerCircuit(ESBlocks.floorCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/floor.png"));
		registerCircuit(ESBlocks.ceilCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/ceil.png"));
		registerCircuit(ESBlocks.equalsCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/equals.png"));
		registerCircuit(ESBlocks.lessCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/less.png"));
		registerCircuit(ESBlocks.moreCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/more.png"));
	}

	/**
	 * This is a public method addons can use to add their circuits to the CircuitWrench
	 * @param toRegister The circuit to be registered
	 * @param icon A path to a valid square icon to represent the circuit. If null, uses the generic missing texture
	 */
	public static void registerCircuit(@Nonnull AbstractTile toRegister, @Nullable ResourceLocation icon){
		if(!MODES.contains(toRegister)){
			MODES.add(toRegister);
			ICONS.add(icon);
		}else{
			Essentials.logger.info("Redundant circuit registration: " + toRegister.getRegistryName());
		}
	}

	protected CircuitWrench(){
		super(new Item.Properties().maxStackSize(1).group(ESItems.TAB_ESSENTIALS));
		String name = "circuit_wrench";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn){
		ItemStack stack = playerIn.getHeldItem(handIn);
		if(playerIn.isCrouching()){
			if(!worldIn.isRemote){
				NetworkHooks.openGui((ServerPlayerEntity) playerIn, UIProvider.INSTANCE);
			}
			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}

		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context){
		BlockState state = context.getWorld().getBlockState(context.getPos());
		BlockState toPlace = MODES.get(context.getItem().getOrCreateTag().getInt(NBT_KEY) % MODES.size()).getDefaultState();

		if(!context.getPlayer().isCrouching() && state.getBlock() instanceof AbstractTile){
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
				if(toPlace.has(ESProperties.HORIZ_FACING)){
					if(state.has(ESProperties.HORIZ_FACING)){
						toPlace = toPlace.with(ESProperties.HORIZ_FACING, state.get(ESProperties.HORIZ_FACING));
					}else{
						toPlace = toPlace.with(ESProperties.HORIZ_FACING, context.getPlayer().getAdjustedHorizontalFacing());
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
