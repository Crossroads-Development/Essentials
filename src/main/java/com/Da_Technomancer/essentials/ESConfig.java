package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ESConfig{

	public static ModConfigSpec.BooleanValue addWrench;

	//	private static ForgeConfigSpec.ConfigValue<List<? extends String>> wrenchTypes;
	public static ModConfigSpec.IntValue brazierRange;
	public static ModConfigSpec.IntValue itemChuteRange;
	public static ModConfigSpec.IntValue fertileSoilRate;
	public static ModConfigSpec.IntValue maxRedstoneRange;
	public static ModConfigSpec.EnumValue<ConfigUtil.NumberTypes> numberDisplay;
	public static ModConfigSpec.IntValue wirelessRange;

	protected static void init(ModContainer modContainer){
		//Client config
		ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
		numberDisplay = clientBuilder.comment("How should very large and small numbers be displayed?", "Options are: NORMAL, SCIENTIFIC, ENGINEERING, and HEX").defineEnum("num_display", ConfigUtil.NumberTypes.SCIENTIFIC);

		ModConfigSpec clientSpec = clientBuilder.build();
		modContainer.registerConfig(ModConfig.Type.CLIENT, clientSpec);

		//Server config
		ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();
//		wrenchTypes = serverBuilder.comment("Item ids for wrench.json items. Should be in format 'modid:itemregistryname', ex. minecraft:apple or essentials:wrench.json").defineList("wrench_types", (List<String>) Arrays.asList(Essentials.MODID + ":wrench.json", "crossroads:liech_wrench", "actuallyadditions:itemlaserwrench", "appliedenergistics2:certus_quartz_wrench", "appliedenergistics2:nether_quartz_wrench", "base:wrench.json", "enderio:itemyetawrench", "extrautils2:wrench.json", "bigreactors:wrench.json", "forestry:wrench.json", "progressiveautomation:wrench.json", "thermalfoundation:wrench.json", "redstonearsenal:tool.wrench_flux", "rftools:smartwrench", "immersiveengineering:tool"), (Object s) -> s instanceof String && ((String) s).contains(":"));
		brazierRange = serverBuilder.comment("Range of the Brazier anti-witch effect", "Set to 0 to disable").defineInRange("brazier_range", 64, 0, 512);
		itemChuteRange = serverBuilder.comment("Maximum Transport Chutes in a line").defineInRange("chute_limit", 16, 0, 128);
		fertileSoilRate = serverBuilder.comment("Percent of normal speed Fertile Soil should work at", "Set to 0 to disable").defineInRange("fertile_rate", 100, 0, 100);
		maxRedstoneRange = serverBuilder.comment("Range of signals through Circuit Wire").defineInRange("redstone_range", 16, 1, 128);
		wirelessRange = serverBuilder.comment("Range of signals through Redstone Receivers/Transmitters").defineInRange("wireless_range", 32, 0, 128);
		addWrench = serverBuilder.comment("Should the Wrench show up in the creative menu?").define("creative_wrench", true);

		ModConfigSpec serverSpec = serverBuilder.build();
		modContainer.registerConfig(ModConfig.Type.SERVER, serverSpec);
	}
}
