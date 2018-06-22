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
	 * A list of all recipes, Item Array are the ingredients, and itemstack is
	 * output. A list for poisonous potato recipes and mashed potato recipes.
	 *
	 * Under no condition is anyone to add support for the Bobo recipes in JEI (or any other recipe helper).
	 */
	public static final ArrayList<Pair<Predicate<ItemStack>[], ItemStack>> brazierBoboRecipes = new ArrayList<Pair<Predicate<ItemStack>[], ItemStack>>();

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
		// Candle Lilypad
		toRegister.add(new ShapelessOreRecipe(null, new ItemStack(EssentialsBlocks.candleLilyPad), Blocks.WATERLILY, "torch"));
		//Fertile Soil
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 0), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', "cropWheat"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 1), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', "cropPotato"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 2), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', "cropCarrot"));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', Items.BEETROOT));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 4), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.OAK.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 5), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.BIRCH.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 6), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.SPRUCE.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 7), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.JUNGLE.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 8), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.ACACIA.getMetadata())));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.fertileSoil, 3, 9), "#$#", "***", "^^^", '#', new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage()), '$', Items.FERMENTED_SPIDER_EYE, '^', "dirt", '*', new ItemStack(Blocks.SAPLING, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata())));
		//Port Extender
		if(OreDictionary.getOres("ingotTin").isEmpty()){
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.portExtender, 1), " # ", "#h#", " # ", '#', "ingotGold", 'h', Blocks.HOPPER));
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.portExtender, 1), " # ", "#h#", " # ", '#', "ingotGold", 'h', EssentialsBlocks.sortingHopper));
		}
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.portExtender, 1), " # ", "#h#", " # ", '#', "ingotTin", 'h', Blocks.HOPPER));
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.portExtender, 1), " # ", "#h#", " # ", '#', "ingotTin", 'h', EssentialsBlocks.sortingHopper));
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
		if(OreDictionary.getOres("stickIron").isEmpty()){
			toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.itemChute, 4), "#$#", "#&#", "#$#", '#', "ingotIron", '$', "stickWood", '&', "ingotGold"));
		}
		toRegister.add(new ShapedOreRecipe(null, new ItemStack(EssentialsBlocks.itemChute, 4), "#$#", "#$#", "#$#", '#', "ingotIron", '$', "stickIron"));
		// Item Chute Port
		toRegister.add(new ShapelessOreRecipe(null, new ItemStack(EssentialsBlocks.itemChutePort, 1), EssentialsBlocks.itemChute, Blocks.IRON_TRAPDOOR));

	}
}
