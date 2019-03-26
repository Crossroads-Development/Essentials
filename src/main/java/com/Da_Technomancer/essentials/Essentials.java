package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.gui.EssentialsGuiHandler;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.tileentities.*;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.TypeReferences;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static com.Da_Technomancer.essentials.Essentials.MODID;

@Mod(MODID)
public final class Essentials{

	public static final String MODID = "essentials";
	public static final String MODNAME = "Essentials";
	public static final Logger logger = LogManager.getLogger(MODNAME);
	public static Essentials instance;


	public Essentials(){
		instance = this;

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonInit);

		MinecraftForge.EVENT_BUS.register(this);
	}

	private void commonInit(FMLCommonSetupEvent e){
		//Pre
		EssentialsConfig.init();
		EssentialsPackets.preInit();
		//Main
		MinecraftForge.EVENT_BUS.register(new EssentialsEventHandlerCommon());
		NetworkRegistry.INSTANCE.registerGuiHandler(Essentials.instance, new EssentialsGuiHandler());
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> e){
		IForgeRegistry<Block> registry = e.getRegistry();
		EssentialsBlocks.init();
		for(Block block : EssentialsBlocks.toRegister){
			registry.register(block);
		}
		EssentialsBlocks.toRegister.clear();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> e){
		IForgeRegistry<Item> registry = e.getRegistry();
		EssentialsItems.init();
		for(Item item : EssentialsItems.toRegister){
			registry.register(item);
		}
		EssentialsItems.toRegister.clear();
	}

	@SubscribeEvent
	public static void registerTileEntities(RegistryEvent.Register<TileEntityType<? extends TileEntity>> e){
		IForgeRegistry<TileEntityType<?>> reg = e.getRegistry();
		register(BrazierTileEntity::new, "brazier", reg);
		register(SlottedChestTileEntity::new, "slotted_chest", reg);
		register(SortingHopperTileEntity::new, "sorting_hopper", reg);
		register(SpeedHopperTileEntity::new, "speed_hopper", reg);
		register(ItemShifterTileEntity::new, "item_shifter", reg);
		register(HopperFilterTileEntity::new, "hopper_filter", reg);
		register(BasicItemSplitterTileEntity::new, "basic_item_splitter", reg);
		register(ItemSplitterTileEntity::new, "item_splitter", reg);
		register(FluidShifterTileEntity::new, "fluid_splitter", reg);
	}

	private static void register(Supplier<? extends TileEntity> cons, String id, IForgeRegistry<TileEntityType<?>> reg){
		Type type = null;

		try{
			//2372 chosen at random
			type = DataFixesManager.getDataFixer().getSchema(DataFixUtils.makeKey(2372)).getChoiceType(TypeReferences.BLOCK_ENTITY, id);
		}catch (IllegalArgumentException ex){
			if(SharedConstants.developmentMode){
				throw ex;
			}
			Essentials.logger.warn("No data fixer registered for block entity {}", id);
		}
		reg.register(TileEntityType.Builder.create(cons).build(type));
	}

	//TODO register recipes
}