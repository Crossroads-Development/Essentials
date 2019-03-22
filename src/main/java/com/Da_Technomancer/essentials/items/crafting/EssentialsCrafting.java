package com.Da_Technomancer.essentials.items.crafting;

import com.Da_Technomancer.essentials.EssentialsConfig;
import com.Da_Technomancer.essentials.blocks.EssentialsBlocks;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.function.Predicate;

public class EssentialsCrafting{

	public static final ArrayList<IRecipe> toRegister = new ArrayList<>();


	/**
	 * A list of all recipes, Item Array are the ingredients, and itemstack is output.
	 * A list for poisonous potato recipes.
	 *
	 * Under no condition is anyone to add support for the Bobo recipes in JEI (or any other recipe helper).
	 */
	public static final ArrayList<Pair<Predicate<ItemStack>[], ItemStack>> brazierBoboRecipes = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public static void init(){

		// Obsidian Cutting Kit
		if(EssentialsConfig.getConfigBool(EssentialsConfig.obsidianKit, false)){
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsItems.obsidianKit, 4), " # ", "#$#", " # ", '$', "obsidian", '#', Items.FLINT));
		}
		// Slotted Chest
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.slottedChest, 1), "###", "$@$", "###", '#', "slabWood", '$', Blocks.TRAPDOOR, '@', "chestWood"));
		// Sorting Hopper
		if(OreDictionary.getOres("ingotCopper").isEmpty()){
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.sortingHopper, 1), "# #", "#&#", " * ", '#', "ingotIron", '&', "chestWood", '*', "ingotGold"));
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.sortingHopper, 1), "#&#", "#*#", '#', "ingotIron", '&', "chestWood", '*', "ingotGold"));
		}
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.sortingHopper, 1), "# #", "#&#", " # ", '#', "ingotCopper", '&', "chestWood"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.sortingHopper, 1), "#&#", "###", '#', "ingotCopper", '&', "chestWood"));
		//Speed Hopper
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.speedHopper, 1), "# #", "#&#", " # ", '#', "ingotGold", '&', "chestWood"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.speedHopper, 1), "#&#", "###", '#', "ingotGold", '&', "chestWood"));
		// Candle Lilypad
		toRegister.add(new ShapelessOreRecipe(null, new ItemStack(EssentialsBlocks.candleLilyPad), Blocks.WATERLILY, "torch"));
		//Fertile Soil
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilWheat, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', "cropWheat"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilCarrot, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', "cropCarrot"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilPotato, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', "cropPotato"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilBeetroot, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', Items.BEETROOT));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilNetherWart, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.SOUL_SAND, '*', Items.NETHER_WART));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilOak, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.OAK.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilBirch, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.BIRCH.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilSpruce, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilJungle, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilDarkOak, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoilAcacia, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.ACACIA.getMetadata())));
		//Hopper Filter
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.hopperFilter, 1), "BRB", " G ", "BRB", 'B', Blocks.BRICK_BLOCK, 'R', "dustRedstone", 'G', "blockGlass"));
		//Saddle
		if(EssentialsConfig.getConfigBool(EssentialsConfig.saddleRecipe, false)){
			toRegister.add(new ShapedOreRecipe(null, Items.SADDLE, "***", "*|*", " - ", '*', "leather", '-', "ingotIron", '|', "string"));
		}
		//Nametag
		if(EssentialsConfig.getConfigBool(EssentialsConfig.nametagRecipe, false)){
			toRegister.add(new ShapedOreRecipe(null, Items.NAME_TAG, "*", "*", "-", '*', "paper", '-', Items.LEAD));
		}
		//Piston
		if(EssentialsConfig.getConfigBool(EssentialsConfig.pistonRecipe, false)){
			toRegister.add(new ShapelessOreRecipe(null, Blocks.PISTON, "cobblestone", "ingotIron", "dustRedstone", "logWood"));
		}
		//Wrench
		if(EssentialsConfig.getConfigBool(EssentialsConfig.addWrench, false)){
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsItems.wrench, 1), "* *", "*|*", " | ", '*', "ingotIron", '|', "stickWood"));
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsItems.wrench, 1), "* *", " | ", " | ", '*', "ingotIron", '|', "stickIron"));
		}
		// Brazier
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.brazier, 1), "###", " $ ", " $ ", '$', "stoneAndesitePolished", '#', "stoneAndesite"));
		// Item Chute
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.itemChute, 6), "I I", "ISI", "I I", 'I', "ingotIron", 'S', "cobblestone"));
		// Item Shifter
		toRegister.add(new ShapelessOreRecipe(null, new ItemStack(EssentialsBlocks.itemShifter, 1), EssentialsBlocks.itemChute, Blocks.DROPPER));
		//Fluid Shifter
		if(OreDictionary.getOres("ingotBronze").isEmpty()){
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fluidShifter, 1), "*|*", "*D*", '*', "ingotIron", '|', EssentialsBlocks.itemChute, 'D', Blocks.DROPPER));
		}
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fluidShifter, 1), "*|*", "*D*", '*', "ingotBronze", '|', EssentialsBlocks.itemChute, 'D', Blocks.DROPPER));
		//Multi-Piston
		toRegister.add(new ShapedOreRecipe(null, EssentialsBlocks.multiPiston, "***", "$#$", "$$$", '*', "ingotTin", '$', "ingotBronze", '#', Blocks.PISTON));
		//Sticky Multi-Piston
		toRegister.add(new ShapedOreRecipe(null, EssentialsBlocks.multiPistonSticky, "***", "$#$", "$$$", '*', "ingotTin", '$', "ingotBronze", '#', Blocks.STICKY_PISTON));
		toRegister.add(new ShapelessOreRecipe(null, EssentialsBlocks.multiPistonSticky, EssentialsBlocks.multiPiston, "slimeball"));
		if(OreDictionary.getOres("ingotBronze").isEmpty() || OreDictionary.getOres("ingotTin").isEmpty()){
			toRegister.add(new ShapedOreRecipe(null, EssentialsBlocks.multiPiston, "***", "$#$", "$$$", '*', "ingotGold", '$', "ingotIron", '#', Blocks.PISTON));
			toRegister.add(new ShapedOreRecipe(null, EssentialsBlocks.multiPistonSticky, "***", "$#$", "$$$", '*', "ingotGold", '$', "ingotIron", '#', Blocks.STICKY_PISTON));
		}
		//Basic Item Splitter
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.basicItemSplitter, 1), "*^*", "&&&", "*^*", '*', "nuggetIron", '^', Blocks.HOPPER, '&', "ingotGold"));
		//Redstone Item Splitter
		toRegister.add(new ShapelessOreRecipe(null, new ItemStack(EssentialsBlocks.itemSplitter, 1), EssentialsBlocks.basicItemSplitter, "dustRedstone", "dustRedstone", "dustRedstone"));
	}
}
