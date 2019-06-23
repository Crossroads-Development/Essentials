package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.*;
import net.minecraft.block.BeetrootBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;

import java.util.ArrayList;

public class EssentialsBlocks{

	public static Brazier brazier;
	public static SlottedChest slottedChest;
	public static SortingHopper sortingHopper;
	public static SpeedHopper speedHopper;
	public static HopperFilter hopperFilter;
	public static FertileSoil fertileSoilWheat;
	public static FertileSoil fertileSoilCarrot;
	public static FertileSoil fertileSoilPotato;
	public static FertileSoil fertileSoilBeetroot;
	public static FertileSoil fertileSoilNetherWart;
	public static FertileSoil fertileSoilOak;
	public static FertileSoil fertileSoilBirch;
	public static FertileSoil fertileSoilSpruce;
	public static FertileSoil fertileSoilJungle;
	public static FertileSoil fertileSoilDarkOak;
	public static FertileSoil fertileSoilAcacia;
	public static FertileSoil fertileSoilBerry;
	public static CandleLilyPad candleLilyPad;
	public static ItemChute itemChute;
	public static ItemShifter itemShifter;
//	public static FluidShifter fluidShifter;
	public static MultiPistonExtend multiPistonExtend;
	public static MultiPistonExtend multiPistonExtendSticky;
	public static MultiPistonBase multiPiston;
	public static MultiPistonBase multiPistonSticky;
	public static BasicItemSplitter basicItemSplitter;
	public static ItemSplitter itemSplitter;

	public static final ArrayList<Block> toRegister = new ArrayList<>();

	public static final Item.Properties itemBlockProp = new Item.Properties().group(EssentialsItems.TAB_ESSENTIALS);

	/**
	 * Registers the item form of a block and the item model.
	 * @param block The block to register
	 * @return The passed block for convenience.
	 */
	public static <T extends Block> T blockAddQue(T block){
		Item item = new BlockItem(block, itemBlockProp).setRegistryName(block.getRegistryName());
		EssentialsItems.toRegister.add(item);
		return block;
	}

	public static void init(){
		brazier = new Brazier();
		slottedChest = new SlottedChest();
		sortingHopper = new SortingHopper();
		speedHopper = new SpeedHopper();
		candleLilyPad = new CandleLilyPad();
		fertileSoilWheat = new FertileSoil("wheat", Blocks.WHEAT.getDefaultState().with(CropsBlock.AGE, 0));
		fertileSoilCarrot = new FertileSoil("carrot", Blocks.CARROTS.getDefaultState().with(CropsBlock.AGE, 0));
		fertileSoilPotato = new FertileSoil("potato", Blocks.POTATOES.getDefaultState().with(CropsBlock.AGE, 0));
		fertileSoilBeetroot = new FertileSoil("beetroot", Blocks.BEETROOTS.getDefaultState().with(BeetrootBlock.BEETROOT_AGE, 0));
		fertileSoilNetherWart = new FertileSoil("netherwart", Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, 0));
		fertileSoilOak = new FertileSoil("oak", Blocks.OAK_SAPLING.getDefaultState());
		fertileSoilBirch = new FertileSoil("birch", Blocks.BIRCH_SAPLING.getDefaultState());
		fertileSoilSpruce = new FertileSoil("spruce", Blocks.SPRUCE_SAPLING.getDefaultState());
		fertileSoilJungle = new FertileSoil("jungle", Blocks.JUNGLE_SAPLING.getDefaultState());
		fertileSoilDarkOak = new FertileSoil("dark_oak", Blocks.DARK_OAK_SAPLING.getDefaultState());
		fertileSoilAcacia = new FertileSoil("acacia", Blocks.ACACIA_SAPLING.getDefaultState());
		fertileSoilBerry = new FertileSoil("berry", Blocks.SWEET_BERRY_BUSH.getDefaultState());
		hopperFilter = new HopperFilter();
		itemChute = new ItemChute();
		itemShifter = new ItemShifter();
		//TODO fluidShifter = new FluidShifter();
		multiPistonExtend = new MultiPistonExtend(false);
		multiPistonExtendSticky = new MultiPistonExtend(true);
		multiPiston = new MultiPistonBase(false);
		multiPistonSticky = new MultiPistonBase(true);
		basicItemSplitter = new BasicItemSplitter();
		itemSplitter = new ItemSplitter();
	}
}
