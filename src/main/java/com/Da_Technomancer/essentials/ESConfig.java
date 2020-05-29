package com.Da_Technomancer.essentials;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ESConfig{

	/**
	 * A common style applied to "quip" lines in tooltips
	 */
	public static final Style TT_QUIP = new Style().setColor(TextFormatting.AQUA).setItalic(true);

	public static ForgeConfigSpec.BooleanValue addWrench;

//	private static ForgeConfigSpec.ConfigValue<List<? extends String>> wrenchTypes;
	public static ForgeConfigSpec.IntValue brazierRange;
	public static ForgeConfigSpec.IntValue itemChuteRange;
	public static ForgeConfigSpec.DoubleValue fertileSoilRate;
	public static ForgeConfigSpec.IntValue maxRedstoneRange;
	public static ForgeConfigSpec.EnumValue<NumberTypes> numberDisplay;
	public static ForgeConfigSpec.IntValue wirelessRange;

	private static ForgeConfigSpec clientSpec;
	private static ForgeConfigSpec serverSpec;

	protected static void init(){
		//Client config
		ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
		addWrench = clientBuilder.worldRestart().comment("Should the Wrench show up in the creative menu?").define("creative_wrench", true);
		numberDisplay = clientBuilder.comment("How should very large and small numbers be displayed?", "Options are: NORMAL, SCIENTIFIC, ENGINEERING, and HEX").defineEnum("num_display", NumberTypes.SCIENTIFIC);

		clientSpec = clientBuilder.build();
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);

		//Server config
		ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
//		wrenchTypes = serverBuilder.comment("Item ids for wrench.json items. Should be in format 'modid:itemregistryname', ex. minecraft:apple or essentials:wrench.json").defineList("wrench_types", (List<String>) Arrays.asList(Essentials.MODID + ":wrench.json", "crossroads:liech_wrench", "actuallyadditions:itemlaserwrench", "appliedenergistics2:certus_quartz_wrench", "appliedenergistics2:nether_quartz_wrench", "base:wrench.json", "enderio:itemyetawrench", "extrautils2:wrench.json", "bigreactors:wrench.json", "forestry:wrench.json", "progressiveautomation:wrench.json", "thermalfoundation:wrench.json", "redstonearsenal:tool.wrench_flux", "rftools:smartwrench", "immersiveengineering:tool"), (Object s) -> s instanceof String && ((String) s).contains(":"));
		brazierRange = serverBuilder.comment("Range of the Brazier anti-witch effect", "Set to 0 to disable").defineInRange("brazier_range", 64, 0, 512);
		itemChuteRange = serverBuilder.comment("Maximum Transport Chutes in a line").defineInRange("chute_limit", 16, 0, 128);
		fertileSoilRate = serverBuilder.comment("Percent of normal speed Fertile Soil should work at", "Set to 0 to disable").defineInRange("fertile_rate", 100D, 0, 100);
		maxRedstoneRange = serverBuilder.comment("Range of signals through Circuit Wire").defineInRange("redstone_range", 16, 1, 128);
		wirelessRange = serverBuilder.comment("Range of signals through Redstone Receivers/Transmitters").defineInRange("wireless_range", 32, 0, 128);

		serverSpec = serverBuilder.build();
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
	}

	protected static void load(){
		CommentedFileConfig clientConfig = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve(Essentials.MODID + "-client.toml")).sync().autosave().writingMode(WritingMode.REPLACE).build();
		clientConfig.load();
		clientSpec.setConfig(clientConfig);

		CommentedFileConfig serverConfig = CommentedFileConfig.builder(FMLPaths.CONFIGDIR.get().resolve(Essentials.MODID + "-server.toml")).sync().autosave().writingMode(WritingMode.REPLACE).build();
		serverConfig.load();
		serverSpec.setConfig(serverConfig);
	}

	private static final Tag<Item> WRENCH = new ItemTags.Wrapper(new ResourceLocation("forge", "wrench"));

	/**
	 * @param stack The stack to test
	 * @return Whether this item is considered a wrench
	 */
	public static boolean isWrench(ItemStack stack){
		//Essentials prefers wrenches defined via the forge:item/wrench.json, but will also check tooltypes- which some mods use to define their wrench
		return WRENCH.contains(stack.getItem()) || stack.getToolTypes().contains(ToolType.get("wrench"));
	}

	/**
	 * Formats floating point values for display
	 * @param value The value to format
	 * @param format The format to conform the value to. Uses the value in the config if null.
	 * @return The formatted string version, for display
	 */
	public static String formatFloat(float value, @Nullable NumberTypes format){
		if(format == null){
			format = numberDisplay.get();
		}
		switch(format){
			case SCIENTIFIC:
				float absValue = Math.abs(value);
				if(absValue == 0){
					return "0";
				}
				if(absValue < 1000 && absValue >= 0.0005F){
					return trimTrail(Math.round(value * 1000F) / 1000F);
				}

				return scientific.format(value);
			case ENGINEERING:
				float absoValue = Math.abs(value);
				if(absoValue == 0){
					return "0";
				}
				if(absoValue < 1000 && absoValue >= 0.0005F){
					return trimTrail(Math.round(value * 1000F) / 1000F);
				}

				return engineering.format(value);
			case HEX:
				//This option exists mainly for debugging. It shows the entire hex definition of the float value
				return Float.toHexString(value);
			case NORMAL:
			default:
				return Float.toString(value);
		}
	}

	private static String trimTrail(float valFloat){
		String val = Float.toString(valFloat);
		//Removes the .0 java appends to string representations of integer-valued floats
		while(val.contains(".") && (val.endsWith("0") || val.endsWith("."))){
			val = val.substring(0, val.length() - 2);
		}
		return val;
	}

	private static final NumberFormat scientific = new DecimalFormat("0.###E0");
	private static final NumberFormat engineering = new DecimalFormat("##0.###E0");

	public enum NumberTypes{

		NORMAL(),//Java default
		SCIENTIFIC(),//Scientific notation when magnitude outside of 0.001-1000
		ENGINEERING(),//Engineering notation when magnitude outside of 0.001-1000
		HEX()//Display the raw float hexadecimal. This exists mainly for debugging. You want this? WHAT IS WRONG WITH YOU?
	}
}
