package com.Da_Technomancer.essentials;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

public class EssentialsConfig{

	public static ForgeConfigSpec.BooleanValue addWrench;

	private static ForgeConfigSpec.ConfigValue<List<? extends String>> wrenchTypes;
	public static ForgeConfigSpec.IntValue brazierRange;
	public static ForgeConfigSpec.IntValue itemChuteRange;
	public static ForgeConfigSpec.DoubleValue fertileSoilRate;

	protected static void init(){
		ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

		addWrench = clientBuilder.worldRestart().translation("creative_wrench").define("creative_wrench", true);

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientBuilder.build());

		ForgeConfigSpec.Builder serverBuilder = new ForgeConfigSpec.Builder();
		wrenchTypes = serverBuilder.comment("Item ids for wrench items. Should be in format 'modid:itemregistryname', ex. minecraft:apple or essentials:wrench").translation("wrench_types").defineList("wrench_types", (List<String>) Arrays.asList(Essentials.MODID + ":wrench", "crossroads:liech_wrench", "actuallyadditions:itemlaserwrench", "appliedenergistics2:certus_quartz_wrench", "appliedenergistics2:nether_quartz_wrench", "base:wrench", "enderio:itemyetawrench", "extrautils2:wrench", "bigreactors:wrench", "forestry:wrench", "progressiveautomation:wrench", "thermalfoundation:wrench", "redstonearsenal:tool.wrench_flux", "rftools:smartwrench", "immersiveengineering:tool"), (Object s) -> s instanceof String && ((String) s).contains(":"));
		brazierRange = serverBuilder.comment("Set to 0 to disable").translation("brazier_range").defineInRange("brazier_range", 64, 0, 512);
		itemChuteRange = serverBuilder.translation("chute_limit").defineInRange("chute_limit", 16, 0, 128);
		fertileSoilRate = serverBuilder.comment("Set to 0 to disable").translation("fertile_rate").defineInRange("fertile_rate", 100D, 0, 100);

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverBuilder.build());
	}

	/**
	 * @param stack The stack to test
	 * @return Whether this item is considered a wrench
	 */
	public static boolean isWrench(ItemStack stack){
		if(stack.isEmpty()){
			return false;
		}
		ResourceLocation loc = stack.getItem().getRegistryName();
		if(loc == null){
			return false;
		}
		String name = loc.toString();
		for(String s : wrenchTypes.get()){
			if(name.equals(s)){
				return true;
			}
		}
		return false;
	}
}
