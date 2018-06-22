package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.gui.EssentialsGuiHandler;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import com.Da_Technomancer.essentials.items.crafting.EssentialsCrafting;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.tileentities.EssentialsTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber()
public class CommonProxy{

	protected void preInit(FMLPreInitializationEvent e){
		EssentialsConfig.init(e);
		EssentialsTileEntity.init();
		EssentialsPackets.preInit();
	}

	protected void init(FMLInitializationEvent e){
		MinecraftForge.EVENT_BUS.register(new EssentialsEventHandlerCommon());
		NetworkRegistry.INSTANCE.registerGuiHandler(Essentials.instance, new EssentialsGuiHandler());

		EssentialsConfig.config.save();
	}

	protected void postInit(FMLPostInitializationEvent e){

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
	public static void registerRecipes(RegistryEvent.Register<IRecipe> e){
		IForgeRegistry<IRecipe> registry = e.getRegistry();
		EssentialsCrafting.init();
		for(IRecipe recipe : EssentialsCrafting.toRegister){
			if(recipe.getRegistryName() == null){
				ResourceLocation rawLoc = new ResourceLocation(Essentials.MODID, recipe.getRecipeOutput().getItem().getRegistryName().getResourcePath());
				ResourceLocation adjusted = rawLoc;
				int i = 0;
				while(CraftingManager.REGISTRY.containsKey(adjusted)){
					adjusted = new ResourceLocation(Essentials.MODID, rawLoc.getResourcePath() + '_' + i);
					i++;
				}
				recipe.setRegistryName(adjusted);
			}
			registry.register(recipe);
		}
		EssentialsCrafting.toRegister.clear();
	}

	/**
	 * These missing mappings events will be removed eventually. 
	 */
	@SubscribeEvent
	public static void missingBlockMapping(RegistryEvent.MissingMappings<Block> event) {
		for(RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()){
			if(mapping.key.getResourceDomain().equals("crossroads")){
				Essentials.logger.info("Repairing missing mapping: " + mapping.key.toString());
				switch(mapping.key.getResourcePath().toLowerCase()){
					case ("candle_lilypad"):
						mapping.remap(EssentialsBlocks.candleLilyPad);
						break;
					case ("fertile_soil"):
						mapping.remap(EssentialsBlocks.fertileSoil);
						break;
					case ("slotted_chest"):
						mapping.remap(EssentialsBlocks.slottedChest);
						break;
					case ("sorting_hopper"):
						mapping.remap(EssentialsBlocks.sortingHopper);
						break;
					case ("port_extender"):
						mapping.remap(EssentialsBlocks.portExtender);
						break;
					case ("brazier"):
						mapping.remap(EssentialsBlocks.brazier);
						break;
					case ("item_chute"):
						mapping.remap(EssentialsBlocks.itemChute);
						break;
					case ("item_chute_port"):
						mapping.remap(EssentialsBlocks.itemChutePort);
						break;
				}
			}
		}
	}

	@SubscribeEvent
	public static void missingItemMapping(RegistryEvent.MissingMappings<Item> event) {
		for(RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()){
			if(mapping.key.getResourceDomain().equals("crossroads")){
				Essentials.logger.info("Repairing missing mapping: " + mapping.key.toString());
				switch(mapping.key.getResourcePath().toLowerCase()){
					case ("candle_lilypad"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.candleLilyPad));
						break;
					case ("fertile_soil"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.fertileSoil));
						break;
					case ("slotted_chest"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.slottedChest));
						break;
					case ("sorting_hopper"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.sortingHopper));
						break;
					case ("obsidian_cutting_kit"):
						mapping.remap(EssentialsItems.obsidianKit);
						break;
					case ("port_extender"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.portExtender));
						break;
					case ("wrench"):
						mapping.remap(EssentialsItems.wrench);
						break;
					case ("brazier"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.brazier));
						break;
					case ("item_chute"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.itemChute));
						break;
					case ("item_chute_port"):
						mapping.remap(Item.getItemFromBlock(EssentialsBlocks.itemChutePort));
						break;
				}
			}
		}
	}
}
