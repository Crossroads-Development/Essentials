package com.Da_Technomancer.essentials.items;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.redstone.IWireConnect;
import com.Da_Technomancer.essentials.api.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.api.ESProperties;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractTile;
import com.Da_Technomancer.essentials.gui.container.CircuitWrenchContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CircuitWrench extends Item{

	/**
	 * Public for read-only; Modify using registerCircuit()
	 */
	public static final ArrayList<IWireConnect> MODES = new ArrayList<>(39);
	/**
	 * Public for read-only; Modify using registerCircuit()
	 */
	public static final ArrayList<ResourceLocation> ICONS = new ArrayList<>(39);

	public static final String NBT_KEY = Essentials.MODID + ":mode";
	private static final TagKey<Item> COMPONENT_TAG = ItemTags.create(new ResourceLocation(Essentials.MODID, "circuit_components"));

	static{
		RedstoneUtil.registerCircuit(ESBlocks.wireCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire.png"));
		RedstoneUtil.registerCircuit(ESBlocks.wireJunctionCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/wire_junction.png"));
		RedstoneUtil.registerCircuit(ESBlocks.interfaceCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/interface.png"));
		RedstoneUtil.registerCircuit(ESBlocks.readerCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/reader.png"));
		RedstoneUtil.registerCircuit(ESBlocks.consCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/constant.png"));
		RedstoneUtil.registerCircuit(ESBlocks.notCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/not.png"));
		RedstoneUtil.registerCircuit(ESBlocks.andCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/and.png"));
		RedstoneUtil.registerCircuit(ESBlocks.orCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/or.png"));
		RedstoneUtil.registerCircuit(ESBlocks.xorCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/xor.png"));
		RedstoneUtil.registerCircuit(ESBlocks.sumCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/sum.png"));
		RedstoneUtil.registerCircuit(ESBlocks.difCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/dif.png"));
		RedstoneUtil.registerCircuit(ESBlocks.prodCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/prod.png"));
		RedstoneUtil.registerCircuit(ESBlocks.quotCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/quot.png"));
		RedstoneUtil.registerCircuit(ESBlocks.invCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/inv.png"));
		RedstoneUtil.registerCircuit(ESBlocks.moduloCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/modulo.png"));
		RedstoneUtil.registerCircuit(ESBlocks.powCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pow.png"));
		RedstoneUtil.registerCircuit(ESBlocks.logCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/log.png"));
		RedstoneUtil.registerCircuit(ESBlocks.sinCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/sin.png"));
		RedstoneUtil.registerCircuit(ESBlocks.cosCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/cos.png"));
		RedstoneUtil.registerCircuit(ESBlocks.tanCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/tan.png"));
		RedstoneUtil.registerCircuit(ESBlocks.asinCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/asin.png"));
		RedstoneUtil.registerCircuit(ESBlocks.acosCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/acos.png"));
		RedstoneUtil.registerCircuit(ESBlocks.atanCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/atan.png"));
		RedstoneUtil.registerCircuit(ESBlocks.maxCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/max.png"));
		RedstoneUtil.registerCircuit(ESBlocks.minCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/min.png"));
		RedstoneUtil.registerCircuit(ESBlocks.roundCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/round.png"));
		RedstoneUtil.registerCircuit(ESBlocks.floorCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/floor.png"));
		RedstoneUtil.registerCircuit(ESBlocks.ceilCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/ceil.png"));
		RedstoneUtil.registerCircuit(ESBlocks.equalsCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/equals.png"));
		RedstoneUtil.registerCircuit(ESBlocks.lessCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/less.png"));
		RedstoneUtil.registerCircuit(ESBlocks.moreCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/more.png"));
		RedstoneUtil.registerCircuit(ESBlocks.absCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/abs.png"));
		RedstoneUtil.registerCircuit(ESBlocks.timerCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/timer.png"));
		RedstoneUtil.registerCircuit(ESBlocks.delayCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/delay.png"));
		RedstoneUtil.registerCircuit(ESBlocks.pulseCircuitRising, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pulse_rising.png"));
		RedstoneUtil.registerCircuit(ESBlocks.pulseCircuitFalling, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pulse_falling.png"));
		RedstoneUtil.registerCircuit(ESBlocks.pulseCircuitDual, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/pulse_dual.png"));
		RedstoneUtil.registerCircuit(ESBlocks.signCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/sign.png"));
		RedstoneUtil.registerCircuit(ESBlocks.dCounterCircuit, new ResourceLocation(Essentials.MODID, "textures/gui/circuit/d_counter.png"));
	}

	protected CircuitWrench(){
		super(new Item.Properties().stacksTo(1).tab(ESItems.TAB_ESSENTIALS));
		String name = "circuit_wrench";
		ESItems.toRegister.put(name, this);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn){
		ItemStack stack = playerIn.getItemInHand(handIn);
		if(playerIn.isCrouching()){
			if(!worldIn.isClientSide){
				NetworkHooks.openScreen((ServerPlayer) playerIn, UIProvider.INSTANCE);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}

		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext context){
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		BlockState toPlace = MODES.get(context.getItemInHand().getOrCreateTag().getInt(NBT_KEY) % MODES.size()).wireAsBlock().defaultBlockState();

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
							if(stack.is(COMPONENT_TAG)){
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
						ItemStack given = new ItemStack(ForgeRegistries.ITEMS.tags().getTag(COMPONENT_TAG).getRandomElement(context.getLevel().random).orElse(Items.QUARTZ), 1);
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
					context.getPlayer().displayClientMessage(Component.translatable("tt.essentials.circuit_wrench.quartz"), true);
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
		tooltip.add(Component.translatable("tt.essentials.circuit_wrench_setting").setStyle(style).append(Component.translatable(MODES.get(mode).wireAsBlock().getDescriptionId())));
		tooltip.add(Component.translatable("tt.essentials.circuit_wrench_info"));
		tooltip.add(Component.translatable("tt.essentials.circuit_wrench_change_mode"));
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
			return Component.translatable("container.circuit_wrench");
		}
	}
}
