package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.blocks.redstone.*;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegisterEvent;

import java.util.HashMap;
import java.util.Map;

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
	public static InterfaceCircuit interfaceCircuit;
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
	public static AbstractCircuit absCircuit;
	public static AbstractCircuit signCircuit;
	public static ReaderCircuit readerCircuit;
	public static TimerCircuit timerCircuit;
	public static RedstoneTransmitter redstoneTransmitter;
	public static RedstoneReceiver redstoneReceiver;
	public static AnalogLamp analogLamp;
	public static DelayCircuit delayCircuit;
	public static PulseCircuit pulseCircuitRising;
	public static PulseCircuit pulseCircuitFalling;
	public static PulseCircuit pulseCircuitDual;
	public static DCounterCircuit dCounterCircuit;
	public static DecorativeBlock bricksIron;
	public static DecorativeBlock bricksGold;
	public static DecorativeBlock bricksTin;
	public static DecorativeBlock bricksBronze;
	public static DecorativeBlock bricksCopshowium;

	private static final HashMap<String, Block> toRegister = new HashMap<>();

	public static final Item.Properties itemBlockProp = new Item.Properties();

	public static BlockBehaviour.Properties getMetalProperty(){
		return BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(3).requiresCorrectToolForDrops();
	}

	public static BlockBehaviour.Properties getRockProperty(){
		return BlockBehaviour.Properties.of(Material.STONE).strength(3).requiresCorrectToolForDrops().sound(SoundType.STONE);
	}

	/**
	 * Queues up a block to be registered, along with an itemblock added to the creative tab
	 * @param regName Block registry name (without essentials: prefix)
	 * @param block Block
	 * @return The block
	 * @param <T> Block class
	 */
	public static <T extends Block> T queueForRegister(String regName, T block){
		return queueForRegister(regName, block, true);
	}

	/**
	 * Queues up a block to be registered, optionally along with an itemblock added to the creative tab
	 * @param regName Block registry name (without essentials: prefix)
	 * @param block Block
	 * @param itemBlock Whether to create and register an associated itemblock
	 * @return The block
	 * @param <T> Block class
	 */
	public static <T extends Block> T queueForRegister(String regName, T block, boolean itemBlock){
		toRegister.put(regName, block);
		if(itemBlock){
			Item item = new BlockItem(block, itemBlockProp);
			ESItems.queueForRegister(regName, item);
		}
		return block;
	}

	public static void init(RegisterEvent.RegisterHelper<Block> helper){
		brazier = new Brazier();
		slottedChest = new SlottedChest();
		sortingHopper = new SortingHopper();
		speedHopper = new SpeedHopper();
		fertileSoilWheat = new FertileSoil("wheat", Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilCarrot = new FertileSoil("carrot", Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilPotato = new FertileSoil("potato", Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilBeetroot = new FertileSoil("beetroot", Blocks.BEETROOTS.defaultBlockState().setValue(BeetrootBlock.AGE, 0), FertileSoil.SeedCategory.CROP);
		fertileSoilNetherWart = new FertileSoil("netherwart", Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 0), FertileSoil.SeedCategory.HELL_CROP);
		fertileSoilOak = new FertileSoil("oak", Blocks.OAK_SAPLING.defaultBlockState(), FertileSoil.SeedCategory.TREE);
		fertileSoilBirch = new FertileSoil("birch", Blocks.BIRCH_SAPLING.defaultBlockState(), FertileSoil.SeedCategory.TREE);
		fertileSoilSpruce = new FertileSoil("spruce", Blocks.SPRUCE_SAPLING.defaultBlockState(), FertileSoil.SeedCategory.TREE);
		fertileSoilJungle = new FertileSoil("jungle", Blocks.JUNGLE_SAPLING.defaultBlockState(), FertileSoil.SeedCategory.TREE);
		fertileSoilDarkOak = new FertileSoil("dark_oak", Blocks.DARK_OAK_SAPLING.defaultBlockState(), FertileSoil.SeedCategory.TREE);
		fertileSoilAcacia = new FertileSoil("acacia", Blocks.ACACIA_SAPLING.defaultBlockState(), FertileSoil.SeedCategory.TREE);
		fertileSoilBerry = new FertileSoil("berry", Blocks.SWEET_BERRY_BUSH.defaultBlockState(), FertileSoil.SeedCategory.BERRY);
		fertileSoilBrownMushroom = new FertileSoil("brown_mushroom", Blocks.BROWN_MUSHROOM.defaultBlockState(), FertileSoil.SeedCategory.MUSHROOM);
		fertileSoilRedMushroom = new FertileSoil("red_mushroom", Blocks.RED_MUSHROOM.defaultBlockState(), FertileSoil.SeedCategory.MUSHROOM);
		hopperFilter = new HopperFilter();
		itemChute = new ItemChute();
		itemShifter = new ItemShifter();
		basicItemSplitter = new BasicItemSplitter();
		itemSplitter = new ItemSplitter();
		fluidShifter = new FluidShifter();
		basicFluidSplitter = new BasicFluidSplitter();
		fluidSplitter = new FluidSplitter();
		multiPistonExtend = new MultiPistonExtend(false);
		multiPistonExtendSticky = new MultiPistonExtend(true);
		multiPiston = new MultiPistonBase(false);
		multiPistonSticky = new MultiPistonBase(true);
		witherCannon = new WitherCannon();
		autoCrafter = new AutoCrafter();
		analogLamp = new AnalogLamp();
		redstoneTransmitter = new RedstoneTransmitter();
		redstoneReceiver = new RedstoneReceiver();
		bricksIron = new DecorativeBlock("bricks_iron", getMetalProperty());
		bricksGold = new DecorativeBlock("bricks_gold", getMetalProperty());
		bricksTin = new DecorativeBlock("bricks_tin", getMetalProperty());
		bricksBronze = new DecorativeBlock("bricks_bronze", getMetalProperty());
		bricksCopshowium = new DecorativeBlock("bricks_copshowium", getMetalProperty());
		candleLilyPad = new CandleLilyPad();//Itemblock registered separately

		//Circuits
		wireCircuit = new WireCircuit();
		wireJunctionCircuit = new WireJunctionCircuit();
		consCircuit = new ConsCircuit();
		//The function outputs will be sanitized regardless, so no sanity-checks are included in the function
		interfaceCircuit = new InterfaceCircuit("interface", (a) -> a, false);
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
		equalsCircuit = new GenericAACircuit("equals", (a0, a1) -> a0.equals(a1) || Math.abs(a0 - a1) / Math.max(a0, a1) <= 0.001F ? 1F : 0);//Checks if the smaller input is within 0.1% of the larger
		lessCircuit = new GenericABCircuit("less", (a, b) -> b < a ? 1F : 0);
		moreCircuit = new GenericABCircuit("more", (a, b) -> b > a ? 1F : 0);
		roundCircuit = new GenericACircuit("round", a1 -> (float) Math.round(a1));
		floorCircuit = new GenericACircuit("floor", (a) -> (float) Math.floor(a));
		ceilCircuit = new GenericACircuit("ceil", (a) -> (float) Math.ceil(a));
		logCircuit = new GenericACircuit("log", (a) -> (float) Math.log10(a));
		moduloCircuit = new GenericABCircuit("modulo", (a, b) -> {
			a = Math.abs(a);
			return ((b % a) + a) % a;
		});//Does the clock modulus, not remainder modulus
		absCircuit = new GenericACircuit("abs", Math::abs);
		signCircuit = new GenericACircuit("sign", Math::signum);
		readerCircuit = new ReaderCircuit();
		timerCircuit = new TimerCircuit();
		delayCircuit = new DelayCircuit();
		pulseCircuitRising = new PulseCircuit(PulseCircuit.Edge.RISING);
		pulseCircuitFalling = new PulseCircuit(PulseCircuit.Edge.FALLING);
		pulseCircuitDual = new PulseCircuit(PulseCircuit.Edge.DUAL);
		dCounterCircuit = new DCounterCircuit();


		for(Map.Entry<String, Block> block : toRegister.entrySet()){
			helper.register(block.getKey(), block.getValue());
		}
		toRegister.clear();
	}
}
