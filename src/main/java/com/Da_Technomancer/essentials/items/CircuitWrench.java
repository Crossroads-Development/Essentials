package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractTile;
import com.Da_Technomancer.essentials.gui.container.CircuitWrenchContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.tags.Tag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CircuitWrench extends Item{

	/**
	 * Public for read-only; Modify using registerCircuit()
	 */
	public static final ArrayList<AbstractTile> MODES = new ArrayList<>(38);
	/**
	 * Public for read-only; Modify using registerCircuit()
	 */
	public static final ArrayList<ResourceLocation> ICONS = new ArrayList<>(38);

	public static final String NBT_KEY = Essentials.MODID + ":mode";
	private static final Tag<Item> COMPONENT_TAG = ItemTags.bind(new ResourceLocation(Essentials.MODID, "circuit_components").toString());

	static{
		registerCircuit(ESBlocks.wireCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire.png"));
		registerCircuit(ESBlocks.wireJunctionCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire_junction.png"));
		registerCircuit(ESBlocks.interfaceCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/interface.png"));
		registerCircuit(ESBlocks.readerCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/reader.png"));
		registerCircuit(ESBlocks.consCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/constant.png"));
		registerCircuit(ESBlocks.notCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/not.png"));
		registerCircuit(ESBlocks.andCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/and.png"));
		registerCircuit(ESBlocks.orCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/or.png"));
		registerCircuit(ESBlocks.xorCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/xor.png"));
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
		registerCircuit(ESBlocks.absCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/abs.png"));
		registerCircuit(ESBlocks.timerCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/timer.png"));
		registerCircuit(ESBlocks.delayCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/delay.png"));
		registerCircuit(ESBlocks.pulseCircuitRising, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pulse_rising.png"));
		registerCircuit(ESBlocks.pulseCircuitFalling, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pulse_falling.png"));
		registerCircuit(ESBlocks.pulseCircuitDual, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pulse_dual.png"));
		registerCircuit(ESBlocks.signCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/sign.png"));
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
			Essentials.logger.warn("Redundant circuit registration: " + toRegister.getRegistryName());
		}
	}

	protected CircuitWrench(){
		super(new Item.Properties().stacksTo(1).tab(ESItems.TAB_ESSENTIALS));
		String name = "circuit_wrench";
		setRegistryName(name);
		ESItems.toRegister.add(this);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn){
		ItemStack stack = playerIn.getItemInHand(handIn);
		if(playerIn.isCrouching()){
			if(!worldIn.isClientSide){
				NetworkHooks.openGui((ServerPlayer) playerIn, UIProvider.INSTANCE);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext context){
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		BlockState toPlace = MODES.get(context.getItemInHand().getOrCreateTag().getInt(NBT_KEY) % MODES.size()).defaultBlockState();

		if(state.getBlock() instanceof AbstractTile){
			if(!context.getPlayer().isShiftKeyDown()){
				//Change circuit type
				AbstractTile worldTile = (AbstractTile) state.getBlock();
				AbstractTile placeTile = (AbstractTile) toPlace.getBlock();

				if(worldTile == placeTile){
					return InteractionResult.SUCCESS;
				}

				boolean allowed = false;
				if(context.getPlayer().isCreative()){
					//Creative mode is free
					allowed = true;
				}else if(placeTile.usesQuartz()){
					if(worldTile.usesQuartz()){
						//Circuit->circuit is free
						allowed = true;
					}else{
						//Have to pay for tile->circuit
						List<ItemStack> playerInv = context.getPlayer().getInventory().items;
						for(ItemStack stack : playerInv){
							if(COMPONENT_TAG.contains(stack.getItem())){
								if(!context.getLevel().isClientSide){
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

					if(worldTile.usesQuartz()){
						//If we downgrade from a circuit to a non-circuit tile (like wire or junction), return a circuit component
						ItemStack given = new ItemStack(COMPONENT_TAG.getRandomElement(context.getLevel().random), 1);
						if(!given.isEmpty()){
							context.getPlayer().addItem(given);
						}
					}
				}

				if(allowed){
					if(toPlace.hasProperty(ESProperties.HORIZ_FACING)){
						if(state.hasProperty(ESProperties.HORIZ_FACING)){
							toPlace = toPlace.setValue(ESProperties.HORIZ_FACING, state.getValue(ESProperties.HORIZ_FACING));
						}else{
							toPlace = toPlace.setValue(ESProperties.HORIZ_FACING, context.getPlayer().getMotionDirection());
						}
					}
					context.getLevel().setBlockAndUpdate(context.getClickedPos(), toPlace);
					return InteractionResult.SUCCESS;
				}else{
					//Print a message saying quartz is needed
					context.getPlayer().displayClientMessage(new TranslatableComponent("tt.essentials.circuit_wrench.quartz"), true);
					return InteractionResult.FAIL;
				}
			}else{
				//Rotate circuit
				if(state.hasProperty(ESProperties.HORIZ_FACING)){
					context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(ESProperties.HORIZ_FACING, state.getValue(ESProperties.HORIZ_FACING).getClockWise()));
					return InteractionResult.SUCCESS;
				}
			}
		}

		return InteractionResult.PASS;
	}

	private static final Style style = Style.EMPTY.applyFormat(ChatFormatting.DARK_RED);

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn){
		int mode = stack.getOrCreateTag().getInt(NBT_KEY) % MODES.size();
		tooltip.add(new TranslatableComponent("tt.essentials.circuit_wrench_setting").setStyle(style).append(new TranslatableComponent(MODES.get(mode).getDescriptionId())));
		tooltip.add(new TranslatableComponent("tt.essentials.circuit_wrench_info"));
		tooltip.add(new TranslatableComponent("tt.essentials.circuit_wrench_change_mode"));
	}

	private static class UIProvider implements MenuProvider{

		private static final UIProvider INSTANCE = new UIProvider();

		@Nullable
		@Override
		public AbstractContainerMenu createMenu(int menuId, Inventory playerInv, Player player){
			return new CircuitWrenchContainer(menuId, playerInv, null);
		}

		@Override
		public Component getDisplayName(){
			return new TranslatableComponent("container.circuit_wrench");
		}
	}
}
