package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class EssentialsBlocks{

	public static Brazier brazier;
	public static SlottedChest slottedChest;
	public static SortingHopper sortingHopper;
	public static PortExtender portExtender;
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
	public static CandleLilyPad candleLilyPad;
	public static ItemChute itemChute;
	public static ItemChutePort itemChutePort;
	public static MultiPistonExtend multiPistonExtend;
	public static MultiPistonExtend multiPistonExtendSticky;
	public static MultiPistonBase multiPiston;
	public static MultiPistonBase multiPistonSticky;

	public static final ArrayList<Block> toRegister = new ArrayList<Block>();

	/**
	 * Registers the item form of a block and the item model.
	 * @param block The block to register
	 * @return The passed block for convenience.
	 */
	public static <T extends Block> T blockAddQue(T block){
		Item item = new ItemBlock(block).setRegistryName(block.getRegistryName());
		EssentialsItems.toRegister.add(item);
		EssentialsItems.itemAddQue(item);
		return block;
	}

	public static void init(){
		brazier = new Brazier();
		slottedChest = new SlottedChest();
		sortingHopper = new SortingHopper();
		candleLilyPad = new CandleLilyPad();
		fertileSoilWheat = new FertileSoil("wheat", Blocks.WHEAT.getDefaultState().withProperty(BlockCrops.AGE, 0));
		fertileSoilCarrot = new FertileSoil("carrot", Blocks.CARROTS.getDefaultState().withProperty(BlockCrops.AGE, 0));
		fertileSoilPotato = new FertileSoil("potato", Blocks.POTATOES.getDefaultState().withProperty(BlockCrops.AGE, 0));
		fertileSoilBeetroot = new FertileSoil("beetroot", Blocks.BEETROOTS.getDefaultState().withProperty(BlockBeetroot.BEETROOT_AGE, 0));
		fertileSoilNetherWart = new FertileSoil("netherwart", Blocks.NETHER_WART.getDefaultState().withProperty(BlockNetherWart.AGE, 0));
		fertileSoilOak = new FertileSoil("oak", Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK));
		fertileSoilBirch = new FertileSoil("birch", Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.BIRCH));
		fertileSoilSpruce = new FertileSoil("spruce", Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.SPRUCE));
		fertileSoilJungle = new FertileSoil("jungle", Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.JUNGLE));
		fertileSoilDarkOak = new FertileSoil("dark_oak", Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.DARK_OAK));
		fertileSoilAcacia = new FertileSoil("acacia", Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.ACACIA));
		portExtender = new PortExtender();
		itemChute = new ItemChute();
		itemChutePort = new ItemChutePort();
		multiPistonExtend = new MultiPistonExtend(false);
		multiPistonExtendSticky = new MultiPistonExtend(true);
		multiPiston = new MultiPistonBase(false);
		multiPistonSticky = new MultiPistonBase(true);
	}
}
