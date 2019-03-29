package com.Da_Technomancer.essentials.items.crafting;

public class EssentialsCrafting{

	/**
	 * A list of all recipes, Item Array are the ingredients, and itemstack is output.
	 * A list for poisonous potato recipes.
	 *
	 * Under no condition is anyone to add support for the Bobo recipes in JEI (or any other recipe helper).
	 */
	//public static final ArrayList<Pair<Predicate<ItemStack>[], ItemStack>> brazierBoboRecipes = new ArrayList<>();

	@SuppressWarnings("unchecked")
	@Deprecated
	public static void init(){
//
//		// Obsidian Cutting Kit
//		//if(EssentialsConfig.getConfigBool(EssentialsConfig.obsidianKit, false)){
//			RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsItems.obsidianKit, 4), " # ", "#$#", " # ", '$', Blocks.OBSIDIAN, '#', Items.FLINT);
//		//}
//		// Slotted Chest
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.slottedChest, 1), "###", "$@$", "###", '#', "slabWood", '$', "trapdoor", '@', "chestWood");
//		// Sorting Hopper
//		//if(OreDictionary.getOres("ingotCopper").isEmpty()){
//			RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.sortingHopper, 1), "# #", "#&#", " * ", '#', "ingotIron", '&', "chestWood", '*', "ingotGold");
//			RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.sortingHopper, 1), "#&#", "#*#", '#', "ingotIron", '&', "chestWood", '*', "ingotGold");
//		//}
//		//RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.sortingHopper, 1), "# #", "#&#", " # ", '#', "ingotCopper", '&', "chestWood");
//		//RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.sortingHopper, 1), "#&#", "###", '#', "ingotCopper", '&', "chestWood");
//		//Speed Hopper
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.speedHopper, 1), "# #", "#&#", " # ", '#', "ingotGold", '&', "chestWood");
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.speedHopper, 1), "#&#", "###", '#', "ingotGold", '&', "chestWood");
//		// Candle Lilypad
//		RecipeJsonGen.addShapelessRecipe(new ItemStack(EssentialsBlocks.candleLilyPad), Blocks.LILY_PAD, Blocks.TORCH);
//		//Fertile Soil
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilWheat, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', Items.WHEAT);
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilCarrot, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', Items.CARROT);
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilPotato, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', Items.POTATO);
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilBeetroot, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', Items.BEETROOT);
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilNetherWart, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.SOUL_SAND, '*', Items.NETHER_WART);
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilOak, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', new ItemStack(Blocks.OAK_SAPLING));
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilBirch, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', new ItemStack(Blocks.BIRCH_SAPLING));
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilSpruce, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', new ItemStack(Blocks.SPRUCE_SAPLING));
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilJungle, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', new ItemStack(Blocks.JUNGLE_SAPLING));
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilDarkOak, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', new ItemStack(Blocks.DARK_OAK_SAPLING));
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fertileSoilAcacia, 3), "#$#", "***", "^^^", '#', new ItemStack(Items.BONE_MEAL, 1), '$', Items.FERMENTED_SPIDER_EYE, '^', Blocks.DIRT, '*', new ItemStack(Blocks.ACACIA_SAPLING));
//		//Hopper Filter
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.hopperFilter, 1), "BRB", " G ", "BRB", 'B', Blocks.BRICKS, 'R', "dustRedstone", 'G', Blocks.GLASS);
//		//Saddle
//		//if(EssentialsConfig.getConfigBool(EssentialsConfig.saddleRecipe, false)){
//			RecipeJsonGen.addShapedRecipe(new ItemStack(Items.SADDLE), "***", "*|*", " - ", '*', Items.LEATHER, '-', "ingotIron", '|', Items.STRING);
//		//}
//		//Nametag
//		//if(EssentialsConfig.getConfigBool(EssentialsConfig.nametagRecipe, false)){
//			RecipeJsonGen.addShapedRecipe(new ItemStack(Items.NAME_TAG), "*", "*", "-", '*', Items.PAPER, '-', Items.LEAD);
//		//}
//		//Piston
//		//if(EssentialsConfig.getConfigBool(EssentialsConfig.pistonRecipe, false)){
//			RecipeJsonGen.addShapelessRecipe(new ItemStack(Blocks.PISTON), "cobblestone", "ingotIron", "dustRedstone", "logWood");
//		//}
//		//Wrench
//		//if(EssentialsConfig.getConfigBool(EssentialsConfig.addWrench, false)){
//			RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsItems.wrench, 1), "* *", "*|*", " | ", '*', "ingotIron", '|', "stickWood");
//			//RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsItems.wrench, 1), "* *", " | ", " | ", '*', "ingotIron", '|', "stickIron");
//		//}
//		// Brazier
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.brazier, 1), "###", " $ ", " $ ", '$', Blocks.POLISHED_ANDESITE, '#', Blocks.ANDESITE);
//		// Item Chute
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.itemChute, 6), "I I", "ISI", "I I", 'I', "ingotIron", 'S', "cobblestone");
//		// Item Shifter
//		RecipeJsonGen.addShapelessRecipe(new ItemStack(EssentialsBlocks.itemShifter, 1), EssentialsBlocks.itemChute, Blocks.DROPPER);
//		//Fluid Shifter
//		//if(OreDictionary.getOres("ingotBronze").isEmpty()){
////			RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fluidShifter, 1), "*|*", "*D*", '*', "ingotIron", '|', EssentialsBlocks.itemChute, 'D', Blocks.DROPPER);
//		//}
////		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.fluidShifter, 1), "*|*", "*D*", '*', "ingotBronze", '|', EssentialsBlocks.itemChute, 'D', Blocks.DROPPER);
//		//Multi-Piston
////		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.multiPiston), "***", "$#$", "$$$", '*', "ingotTin", '$', "ingotBronze", '#', Blocks.PISTON);
//		//Sticky Multi-Piston
////		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.multiPistonSticky), "***", "$#$", "$$$", '*', "ingotTin", '$', "ingotBronze", '#', Blocks.STICKY_PISTON);
//		RecipeJsonGen.addShapelessRecipe(new ItemStack(EssentialsBlocks.multiPistonSticky), EssentialsBlocks.multiPiston, Items.SLIME_BALL);
//		//if(OreDictionary.getOres("ingotBronze").isEmpty() || OreDictionary.getOres("ingotTin").isEmpty()){
//			RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.multiPiston), "***", "$#$", "$$$", '*', "ingotGold", '$', "ingotIron", '#', Blocks.PISTON);
//			RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.multiPistonSticky), "***", "$#$", "$$$", '*', "ingotGold", '$', "ingotIron", '#', Blocks.STICKY_PISTON);
//		//}
//		//Basic Item Splitter
//		RecipeJsonGen.addShapedRecipe(new ItemStack(EssentialsBlocks.basicItemSplitter, 1), "*^*", "&&&", "*^*", '*', "nuggetIron", '^', Blocks.HOPPER, '&', "ingotGold");
//		//Redstone Item Splitter
//		RecipeJsonGen.addShapelessRecipe(new ItemStack(EssentialsBlocks.itemSplitter, 1), EssentialsBlocks.basicItemSplitter, "dustRedstone", "dustRedstone", "dustRedstone");

	}
}
