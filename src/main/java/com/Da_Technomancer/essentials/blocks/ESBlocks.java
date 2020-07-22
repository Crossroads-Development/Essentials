package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.*;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import java.util.ArrayList;

public class ESBlocks{

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
	public static FertileSoil fertileSoilBrownMushroom;
	public static FertileSoil fertileSoilRedMushroom;
	public static CandleLilyPad candleLilyPad;
	public static ItemChute itemChute;
	public static ItemShifter itemShifter;
	public static FluidShifter fluidShifter;
	public static MultiPistonExtend multiPistonExtend;
	public static MultiPistonExtend multiPistonExtendSticky;
	public static MultiPistonBase multiPiston;
	public static MultiPistonBase multiPistonSticky;
	public static BasicItemSplitter basicItemSplitter;
	public static ItemSplitter itemSplitter;
	public static BasicFluidSplitter basicFluidSplitter;
	public static FluidSplitter fluidSplitter;
	public static WitherCannon witherCannon;
	public static AutoCrafter autoCrafter;

	public static WireCircuit wireCircuit;
	public static AbstractCircuit interfaceCircuit;
	public static WireJunctionCircuit wireJunctionCircuit;
	public static AbstractCircuit andCircuit;
	public static ConsCircuit consCircuit;
	public static AbstractCircuit notCircuit;
	public static AbstractCircuit orCircuit;
	public static AbstractCircuit xorCircuit;
	public static AbstractCircuit maxCircuit;
	public static AbstractCircuit minCircuit;
	public static AbstractCircuit sumCircuit;
	public static AbstractCircuit difCircuit;
	public static AbstractCircuit prodCircuit;
	public static AbstractCircuit quotCircuit;
	public static AbstractCircuit powCircuit;
	public static AbstractCircuit invCircuit;
	public static AbstractCircuit sinCircuit;
	public static AbstractCircuit cosCircuit;
	public static AbstractCircuit tanCircuit;
	public static AbstractCircuit asinCircuit;
	public static AbstractCircuit acosCircuit;
	public static AbstractCircuit atanCircuit;
	public static AbstractCircuit equalsCircuit;
	public static AbstractCircuit lessCircuit;
	public static AbstractCircuit moreCircuit;
	public static AbstractCircuit roundCircuit;
	public static AbstractCircuit floorCircuit;
	public static AbstractCircuit ceilCircuit;
	public static AbstractCircuit logCircuit;
	public static AbstractCircuit moduloCircuit;
	public static ReaderCircuit readerCircuit;
	public static RedstoneTransmitter redstoneTransmitter;
	public static RedstoneReceiver redstoneReceiver;

	public static final ArrayList<Block> toRegister = new ArrayList<>();

	public static final Item.Properties itemBlockProp = new Item.Properties().group(ESItems.TAB_ESSENTIALS);

	/**
	 * Registers the item form of a blocks and the item model.
	 * @param block The blocks to register
	 * @return The passed blocks for convenience.
	 */
	public static <T extends Block> T blockAddQue(T block){
		Item item = new BlockItem(block, itemBlockProp).setRegistryName(block.getRegistryName());
		ESItems.toRegister.add(item);
		return block;
	}

	public static void init(){
		brazier = new Brazier();
		slottedChest = new SlottedChest();
		sortingHopper = new SortingHopper();
		speedHopper = new SpeedHopper();
		candleLilyPad = new CandleLilyPad();
		fertileSoilWheat = new FertileSoil("wheat", Blocks.WHEAT.getDefaultState().with(CropsBlock.AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilCarrot = new FertileSoil("carrot", Blocks.CARROTS.getDefaultState().with(CropsBlock.AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilPotato = new FertileSoil("potato", Blocks.POTATOES.getDefaultState().with(CropsBlock.AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilBeetroot = new FertileSoil("beetroot", Blocks.BEETROOTS.getDefaultState().with(BeetrootBlock.BEETROOT_AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilNetherWart = new FertileSoil("netherwart", Blocks.NETHER_WART.getDefaultState().with(NetherWartBlock.AGE, 0), FertileSoil.SeedCategory.HELL_CROP);
		fertileSoilOak = new FertileSoil("oak", Blocks.OAK_SAPLING.getDefaultState(), FertileSoil.SeedCategory.TREE);
		fertileSoilBirch = new FertileSoil("birch", Blocks.BIRCH_SAPLING.getDefaultState(), FertileSoil.SeedCategory.TREE);
		fertileSoilSpruce = new FertileSoil("spruce", Blocks.SPRUCE_SAPLING.getDefaultState(), FertileSoil.SeedCategory.TREE);
		fertileSoilJungle = new FertileSoil("jungle", Blocks.JUNGLE_SAPLING.getDefaultState(), FertileSoil.SeedCategory.TREE);
		fertileSoilDarkOak = new FertileSoil("dark_oak", Blocks.DARK_OAK_SAPLING.getDefaultState(), FertileSoil.SeedCategory.TREE);
		fertileSoilAcacia = new FertileSoil("acacia", Blocks.ACACIA_SAPLING.getDefaultState(), FertileSoil.SeedCategory.TREE);
		fertileSoilBerry = new FertileSoil("berry", Blocks.SWEET_BERRY_BUSH.getDefaultState(), FertileSoil.SeedCategory.BERRY);
		fertileSoilBrownMushroom = new FertileSoil("brown_mushroom", Blocks.BROWN_MUSHROOM.getDefaultState(), FertileSoil.SeedCategory.MUSHROOM);
		fertileSoilRedMushroom = new FertileSoil("red_mushroom", Blocks.RED_MUSHROOM.getDefaultState(), FertileSoil.SeedCategory.MUSHROOM);
		hopperFilter = new HopperFilter();
		itemChute = new ItemChute();
		itemShifter = new ItemShifter();
		fluidShifter = new FluidShifter();
		multiPistonExtend = new MultiPistonExtend(false);
		multiPistonExtendSticky = new MultiPistonExtend(true);
		multiPiston = new MultiPistonBase(false);
		multiPistonSticky = new MultiPistonBase(true);
		basicItemSplitter = new BasicItemSplitter();
		itemSplitter = new ItemSplitter();
		basicFluidSplitter = new BasicFluidSplitter();
		fluidSplitter = new FluidSplitter();
		witherCannon = new WitherCannon();
		autoCrafter = new AutoCrafter();

		wireCircuit = new WireCircuit();
		wireJunctionCircuit = new WireJunctionCircuit();
		consCircuit = new ConsCircuit();
		//The function outputs will be sanitized regardless, so no sanity-checks are included in the function
		interfaceCircuit = new GenericACircuit("interface", (a) -> a);
		andCircuit = new GenericAACircuit("and", (a0, a1) -> a0 > 0 && a1 > 0 ? 1F : 0F);
		notCircuit = new GenericACircuit("not", (a) -> a > 0 ? 0F : 1F);
		orCircuit = new GenericAACircuit("or", (a0, a1) -> a0 > 0 || a1 > 0 ? 1F : 0F);
		xorCircuit = new GenericAACircuit("xor", (a0, a1) -> a0 > 0 ^ a1 > 0 ? 1F : 0F);
		maxCircuit = new GenericAACircuit("max", Math::max);
		minCircuit = new GenericAACircuit("min", Math::min);
		sumCircuit = new GenericAACircuit("sum", Float::sum);
		difCircuit = new GenericABCircuit("dif", (a, b) -> b - a);
		prodCircuit = new GenericAACircuit("prod", (a0, a1) -> a0 * a1);
		quotCircuit = new GenericABCircuit("quot", (a, b) -> b / a);
		powCircuit = new GenericABCircuit("pow", (a, b) -> (float) Math.pow(b, a));
		invCircuit = new GenericACircuit("inv", (a) -> 1F / a);
		sinCircuit = new GenericACircuit("sin", (a) -> (float) Math.sin(a));
		cosCircuit = new GenericACircuit("cos", (a) -> (float) Math.cos(a));
		tanCircuit = new GenericACircuit("tan", (a) -> (float) Math.tan(a));
		asinCircuit = new GenericACircuit("asin", (a) -> (float) Math.asin(a));
		acosCircuit = new GenericACircuit("acos", (a) -> (float) Math.acos(a));
		atanCircuit = new GenericACircuit("atan", (a) -> (float) Math.atan(a));
		equalsCircuit = new GenericAACircuit("equals", (a0, a1) -> a0.equals(a1) || Math.abs(a0 - a1) / Math.max(a0, a1) <= 0.01F ? 1F : 0);//Checks if the smaller input is within 1% of the larger
		lessCircuit = new GenericABCircuit("less", (a, b) -> b < a ? 1F : 0);
		moreCircuit = new GenericABCircuit("more", (a, b) -> b > a ? 1F : 0);
		roundCircuit = new GenericACircuit("round", a1 -> (float) Math.round(a1));
		floorCircuit = new GenericACircuit("floor", (a) -> (float) Math.floor(a));
		ceilCircuit = new GenericACircuit("ceil", (a) -> (float) Math.ceil(a));
		logCircuit = new GenericACircuit("log", (a) -> (float) Math.log10(a));
		moduloCircuit = new GenericABCircuit("modulo", (a, b) -> b % a);
		readerCircuit = new ReaderCircuit();
		redstoneTransmitter = new RedstoneTransmitter();
		redstoneReceiver = new RedstoneReceiver();
	}
}
