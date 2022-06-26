package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.api.ConfigUtil;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

public class ESConfig{

	public static ForgeConfigSpec.BooleanValue addWrench;

	//	private static ForgeConfigSpec.ConfigValue<List<? extends String>> wrenchTypes;
	public static ForgeConfigSpec.IntValue brazierRange;
	public static ForgeConfigSpec.IntValue itemChuteRange;
	public static ForgeConfigSpec.DoubleValue fertileSoilRate;
	public static ForgeConfigSpec.IntValue maxRedstoneRange;
	public static ForgeConfigSpec.EnumValue<ConfigUtil.NumberTypes> numberDisplay;
	public static ForgeConfigSpec.IntValue wirelessRange;

	private static ForgeConfigSpec clientSpec;
	private static ForgeConfigSpec serverSpec;

	protected static void init(){
		//Client config
		ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
		addWrench = clientBuilder.worldRestart().comment("Should the Wrench show up in the creative menu?").define("creative_wrench", true);
		numberDisplay = clientBuilder.comment("How should very large and small numbers be displayed?", "Options are: NORMAL, SCIENTIFIC, ENGINEERING, and HEX").defineEnum("num_display", ConfigUtil.NumberTypes.SCIENTIFIC);

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

}
