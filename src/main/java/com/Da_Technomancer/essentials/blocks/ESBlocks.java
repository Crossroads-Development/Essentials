package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.*;
import com.Da_Technomancer.essentials.items.ESItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ESBlocks{

	public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, Essentials.MODID);

	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<Brazier>> BRAZIER_TYPE = BLOCK_TYPES.register("brazier", singletonBlockType(Brazier::new));
	public static Brazier brazier;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<SlottedChest>> SLOTTED_CHEST_TYPE = BLOCK_TYPES.register("slotted_chest", singletonBlockType(SlottedChest::new));
	public static SlottedChest slottedChest;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<SortingHopper>> SORTING_HOPPER_TYPE = BLOCK_TYPES.register("sorting_hopper", singletonBlockType(SortingHopper::new));
	public static SortingHopper sortingHopper;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<SpeedHopper>> SPEED_HOPPER_TYPE = BLOCK_TYPES.register("speed_hopper", singletonBlockType(SpeedHopper::new));
	public static SpeedHopper speedHopper;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<HopperFilter>> HOPPER_FILTER_TYPE = BLOCK_TYPES.register("hopper_filter", singletonBlockType(HopperFilter::new));
	public static HopperFilter hopperFilter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<FertileSoil>> FERTILE_SOIL_TYPE = BLOCK_TYPES.register("fertile_soil", singletonBlockType(FertileSoil::new));
	public static FertileSoil fertileSoil;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<WaterlilyBlock>> CANDLE_LILYPAD = BLOCK_TYPES.register("candle_lilypad", singletonBlockType(CandleLilyPad::new));
	public static CandleLilyPad candleLilyPad;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<ItemChute>> ITEM_CHUTE_TYPE = BLOCK_TYPES.register("item_chute", singletonBlockType(ItemChute::new));
	public static ItemChute itemChute;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<ItemShifter>> ITEM_SHIFTER_TYPE = BLOCK_TYPES.register("item_shifter", singletonBlockType(ItemShifter::new));
	public static ItemShifter itemShifter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<FluidShifter>> FLUID_SHIFTER_TYPE = BLOCK_TYPES.register("fluid_shifter", singletonBlockType(FluidShifter::new));
	public static FluidShifter fluidShifter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<MultiPistonExtend>> MULTI_PISTON_EXTEND_TYPE = BLOCK_TYPES.register("multi_piston_extend", () -> MultiPistonExtend.CODEC);
	public static MultiPistonExtend multiPistonExtend;
	public static MultiPistonExtend multiPistonExtendSticky;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<MultiPistonBase>> MULTI_PISTON_TYPE = BLOCK_TYPES.register("multi_piston", () -> MultiPistonBase.CODEC);
	public static MultiPistonBase multiPiston;
	public static MultiPistonBase multiPistonSticky;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<BasicItemSplitter>> BASIC_ITEM_SPLITTER_TYPE = BLOCK_TYPES.register("basic_item_splitter", singletonBlockType(BasicItemSplitter::new));
	public static BasicItemSplitter basicItemSplitter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<ItemSplitter>> ITEM_SPLITTER_TYPE = BLOCK_TYPES.register("item_splitter", singletonBlockType(ItemSplitter::new));
	public static ItemSplitter itemSplitter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<BasicFluidSplitter>> BASIC_FLUID_SPLITTER_TYPE = BLOCK_TYPES.register("basic_fluid_splitter", singletonBlockType(BasicFluidSplitter::new));
	public static BasicFluidSplitter basicFluidSplitter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<FluidSplitter>> FLUID_SPLITTER_TYPE = BLOCK_TYPES.register("fluid_splitter", singletonBlockType(FluidSplitter::new));
	public static FluidSplitter fluidSplitter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<WitherCannon>> WITHER_CANNON_TYPE = BLOCK_TYPES.register("wither_cannon", singletonBlockType(WitherCannon::new));
	public static WitherCannon witherCannon;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<WireCircuit>> WIRE_CIRCUIT_TYPE = BLOCK_TYPES.register("wire_circuit", singletonBlockType(WireCircuit::new));
	public static WireCircuit wireCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<InterfaceCircuit>> INTERFACE_CIRCUIT_TYPE = BLOCK_TYPES.register("interface_circuit", singletonBlockType(() -> new InterfaceCircuit("interface", false)));
	public static InterfaceCircuit interfaceCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<WireJunctionCircuit>> WIRE_JUNCTION_CIRCUIT_TYPE = BLOCK_TYPES.register("wire_junction_circuit", singletonBlockType(WireJunctionCircuit::new));
	public static WireJunctionCircuit wireJunctionCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<WireBypassCircuit>> WIRE_BYPASS_CIRCUIT_TYPE = BLOCK_TYPES.register("wire_bypass_circuit", singletonBlockType(WireBypassCircuit::new));
	public static WireBypassCircuit wireBypassCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<ConsCircuit>> CONS_CIRCUIT_TYPE = BLOCK_TYPES.register("cons_circuit", singletonBlockType(ConsCircuit::new));
	public static ConsCircuit consCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<GenericACircuit>> GENERIC_A_CIRCUIT_TYPE = BLOCK_TYPES.register("generic_a_circuit", () -> GenericACircuit.CODEC);
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<GenericAACircuit>> GENERIC_AA_CIRCUIT_TYPE = BLOCK_TYPES.register("generic_aa_circuit", () -> GenericAACircuit.CODEC);
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<GenericABCircuit>> GENERIC_AB_CIRCUIT_TYPE = BLOCK_TYPES.register("generic_ab_circuit", () -> GenericABCircuit.CODEC);
	public static GenericAACircuit andCircuit;
	public static GenericACircuit notCircuit;
	public static GenericAACircuit orCircuit;
	public static GenericAACircuit xorCircuit;
	public static GenericAACircuit maxCircuit;
	public static GenericAACircuit minCircuit;
	public static GenericAACircuit sumCircuit;
	public static GenericABCircuit difCircuit;
	public static GenericAACircuit prodCircuit;
	public static GenericABCircuit quotCircuit;
	public static GenericABCircuit powCircuit;
	public static GenericACircuit invCircuit;
	public static GenericACircuit sinCircuit;
	public static GenericACircuit cosCircuit;
	public static GenericACircuit tanCircuit;
	public static GenericACircuit asinCircuit;
	public static GenericACircuit acosCircuit;
	public static GenericACircuit atanCircuit;
	public static GenericAACircuit equalsCircuit;
	public static GenericABCircuit lessCircuit;
	public static GenericABCircuit moreCircuit;
	public static GenericACircuit roundCircuit;
	public static GenericACircuit floorCircuit;
	public static GenericACircuit ceilCircuit;
	public static GenericACircuit logCircuit;
	public static GenericABCircuit moduloCircuit;
	public static GenericACircuit absCircuit;
	public static GenericACircuit signCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<ReaderCircuit>> READER_CIRCUIT_TYPE = BLOCK_TYPES.register("reader_circuit", singletonBlockType(ReaderCircuit::new));
	public static ReaderCircuit readerCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<TimerCircuit>> TIMER_CIRCUIT_TYPE = BLOCK_TYPES.register("timer_circuit", singletonBlockType(TimerCircuit::new));
	public static TimerCircuit timerCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<RedstoneTransmitter>> REDSTONE_TRANSMITTER_TYPE = BLOCK_TYPES.register("redstone_transmitter", singletonBlockType(RedstoneTransmitter::new));
	public static RedstoneTransmitter redstoneTransmitter;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<RedstoneReceiver>> REDSTONE_RECEIVER_TYPE = BLOCK_TYPES.register("redstone_receiver", singletonBlockType(RedstoneReceiver::new));
	public static RedstoneReceiver redstoneReceiver;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<AnalogLamp>> ANALOG_LAMP_TYPE = BLOCK_TYPES.register("analog_lamp", singletonBlockType(AnalogLamp::new));
	public static AnalogLamp analogLamp;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<DelayCircuit>> DELAY_CIRCUIT_TYPE = BLOCK_TYPES.register("delay_circuit", singletonBlockType(DelayCircuit::new));
	public static DelayCircuit delayCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<PulseCircuit>> PULSE_CIRCUIT_TYPE = BLOCK_TYPES.register("pulse_circuit", () -> PulseCircuit.CODEC);
	public static PulseCircuit pulseCircuitRising;
	public static PulseCircuit pulseCircuitFalling;
	public static PulseCircuit pulseCircuitDual;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<DCounterCircuit>> D_COUNTER_CIRCUIT_TYPE = BLOCK_TYPES.register("d_counter_circuit", singletonBlockType(DCounterCircuit::new));
	public static DCounterCircuit dCounterCircuit;
	public static final DeferredHolder<MapCodec<? extends Block>, MapCodec<DecorativeBlock>> DECORATIVE_TYPE = BLOCK_TYPES.register("decorative", () -> DecorativeBlock.CODEC);
	public static DecorativeBlock bricksIron;
	public static DecorativeBlock bricksGold;
	public static DecorativeBlock bricksTin;
	public static DecorativeBlock bricksBronze;
	public static DecorativeBlock bricksCopshowium;

	private static final HashMap<String, Block> toRegister = new HashMap<>();

	public static final Item.Properties itemBlockProp = new Item.Properties();

	public static BlockBehaviour.Properties getMetalProperty(){
		return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(3).requiresCorrectToolForDrops();
	}

	public static BlockBehaviour.Properties getRockProperty(){
		return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3).requiresCorrectToolForDrops().sound(SoundType.STONE);
	}

	public static <T extends Block> Supplier<MapCodec<T>> singletonBlockType(Supplier<T> blockConstructor){
		return () -> MapCodec.unit(blockConstructor);
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

	public static void init(IEventBus modBus){
		BLOCK_TYPES.register(modBus);
	}

	public static void registerBlocks(RegisterEvent.RegisterHelper<Block> helper){
		brazier = new Brazier();
		slottedChest = new SlottedChest();
		sortingHopper = new SortingHopper();
		speedHopper = new SpeedHopper();
		fertileSoil = new FertileSoil();
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
		wireBypassCircuit = new WireBypassCircuit();
		consCircuit = new ConsCircuit();
		//The function outputs will be sanitized regardless, so no sanity-checks are included in the function
		interfaceCircuit = new InterfaceCircuit("interface", false);
		andCircuit = new GenericAACircuit("and");
		notCircuit = new GenericACircuit("not");
		orCircuit = new GenericAACircuit("or");
		xorCircuit = new GenericAACircuit("xor");
		maxCircuit = new GenericAACircuit("max");
		minCircuit = new GenericAACircuit("min");
		sumCircuit = new GenericAACircuit("sum");
		difCircuit = new GenericABCircuit("dif");
		prodCircuit = new GenericAACircuit("prod");
		quotCircuit = new GenericABCircuit("quot");
		powCircuit = new GenericABCircuit("pow");
		invCircuit = new GenericACircuit("inv");
		sinCircuit = new GenericACircuit("sin");
		cosCircuit = new GenericACircuit("cos");
		tanCircuit = new GenericACircuit("tan");
		asinCircuit = new GenericACircuit("asin");
		acosCircuit = new GenericACircuit("acos");
		atanCircuit = new GenericACircuit("atan");
		equalsCircuit = new GenericAACircuit("equals");
		lessCircuit = new GenericABCircuit("less");
		moreCircuit = new GenericABCircuit("more");
		roundCircuit = new GenericACircuit("round");
		floorCircuit = new GenericACircuit("floor");
		ceilCircuit = new GenericACircuit("ceil");
		logCircuit = new GenericACircuit("log");
		moduloCircuit = new GenericABCircuit("modulo");
		absCircuit = new GenericACircuit("abs");
		signCircuit = new GenericACircuit("sign");
		readerCircuit = new ReaderCircuit();
		timerCircuit = new TimerCircuit();
		delayCircuit = new DelayCircuit();
		pulseCircuitRising = new PulseCircuit(PulseCircuit.Edge.RISING);
		pulseCircuitFalling = new PulseCircuit(PulseCircuit.Edge.FALLING);
		pulseCircuitDual = new PulseCircuit(PulseCircuit.Edge.DUAL);
		dCounterCircuit = new DCounterCircuit();

		for(Map.Entry<String, Block> block : toRegister.entrySet()){
			helper.register(ResourceLocation.fromNamespaceAndPath(Essentials.MODID, block.getKey()), block.getValue());
		}
		toRegister.clear();
	}
}
