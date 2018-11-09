package com.Da_Technomancer.essentials;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;

public final class EssentialsConfig{

	public static Configuration config;

	public static Property addWrench;
	public static Property wrenchTypes;
	public static Property saddleRecipe;
	public static Property nametagRecipe;
	public static Property obsidianKit;
	public static Property pistonRecipe;
	public static Property brazierRange;
	public static Property itemChuteRotary;
	public static Property fertileSoilRate;

	private static final ArrayList<Property> SYNCED_PROPERTIES = new ArrayList<>();
	public static NBTTagCompound syncPropNBT;

	private static final String CAT_INTERNAL = "Internal";
	private static final String CAT_RECIPES = "Recipes";

	protected static void init(FMLPreInitializationEvent e){

		config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();

		saddleRecipe = config.get(CAT_RECIPES, "Add a recipe for saddles (Default true)?", true);
		nametagRecipe = config.get(CAT_RECIPES, "Add a recipe for nametags (Default true)?", true);
		pistonRecipe = config.get(CAT_RECIPES, "Add a recipe for pistons (Default true)?", true);
		obsidianKit = config.get(CAT_RECIPES, "Add a recipe for obsidian cutting kits (Default true)?", true);
		SYNCED_PROPERTIES.add(addWrench = config.get(CAT_INTERNAL, "Show the wrench in the creative menu? (Default true)", true));
		SYNCED_PROPERTIES.add(wrenchTypes = config.get(CAT_INTERNAL, "Item ids for wrench items. Should be in format 'modid:itemregistryname', ex. minecraft:apple or essentials:wrench.", new String[] {Essentials.MODID + ":wrench", "crossroads:liech_wrench", "actuallyadditions:itemlaserwrench", "appliedenergistics2:certus_quartz_wrench", "appliedenergistics2:nether_quartz_wrench", "base:wrench", "enderio:itemyetawrench", "extrautils2:wrench", "bigreactors:wrench", "forestry:wrench", "progressiveautomation:wrench", "thermalfoundation:wrench", "redstonearsenal:tool.wrench_flux", "rftools:smartwrench", "immersiveengineering:tool"}));
		SYNCED_PROPERTIES.add(brazierRange = config.get(CAT_INTERNAL, "Range of the brazier witch-blocking effect. Set to 0 to disable. (Default 64)", 64, null, 0, 512));
		SYNCED_PROPERTIES.add(itemChuteRotary = config.get(CAT_INTERNAL, "Should the Item Chute require Crossroads rotary power when Crossroads is installed? (Default false)", false));
		fertileSoilRate = config.get(CAT_INTERNAL, "% chance of Fertile Soil creating a plant per growth tick. (Default 100)", 100D, "0 to 100", 0, 100);
	}

	/**
	 * Returns the value of the Property, but if client side it will return the server side value if the Property is in the list SYNCED_PROPERTIES.
	 * 
	 * @param prop The property to get the value of.
	 * @param client Whether this is on the virtual client. Pass this param. true if unknown.
	 * @return The config value.
	 */
	public static boolean getConfigBool(Property prop, boolean client){
		if(prop.getType() != Property.Type.BOOLEAN || prop.isList()){
			throw new ClassCastException(Essentials.MODID + ": Incorrect config property type.");
		}
		//The NBT should never be null, but just to be safe, it is better to have the wrong client config than crash.
		if(!client || syncPropNBT == null){
			return prop.getBoolean();
		}
		int index = SYNCED_PROPERTIES.indexOf(prop);
		if(index == -1){
			return prop.getBoolean();
		}
		return syncPropNBT.getBoolean("p_" + index);
	}

	/**
	 * Returns the value of the Property, but if client side it will return the server side value if the Property is in the list SYNCED_PROPERTIES.
	 *
	 * @param prop The property to get the value of.
	 * @param client Whether this is on the virtual client. Pass this param. true if unknown.
	 * @return The config value.
	 */
	public static double getConfigDouble(Property prop, boolean client){
		if(prop.getType() != Property.Type.DOUBLE || prop.isList()){
			throw new ClassCastException(Essentials.MODID + ": Incorrect config property type.");
		}
		//The NBT should never be null, but just to be safe, it is better to have the wrong client config than crash.
		if(!client || syncPropNBT == null){
			return prop.getDouble();
		}
		int index = SYNCED_PROPERTIES.indexOf(prop);
		if(index == -1){
			return prop.getDouble();
		}
		return syncPropNBT.getDouble("p_" + index);
	}

	/**
	 * Returns the value of the Property, but if client side it will return the server side value if the Property is in the list SYNCED_PROPERTIES.
	 * 
	 * @param prop The property to get the value of.
	 * @param client Whether this is on the virtual client. Pass this param. true if unknown.
	 * @return The config value.
	 */
	public static int getConfigInt(Property prop, boolean client){
		if(prop.getType() != Property.Type.INTEGER || prop.isList()){
			throw new ClassCastException(Essentials.MODID + ": Incorrect config property type.");
		}
		//The NBT should never be null, but just to be safe, it is better to have the wrong client config than crash.
		if(!client || syncPropNBT == null){
			return prop.getInt();
		}
		int index = SYNCED_PROPERTIES.indexOf(prop);
		if(index == -1){
			return prop.getInt();
		}
		return syncPropNBT.getInteger("p_" + index);
	}

	/**
	 * Returns the value of the Property, but if client side it will return the server side value if the Property is in the list SYNCED_PROPERTIES.
	 * 
	 * @param prop The property to get the value of.
	 * @param client Whether this is on the virtual client. Pass this param. true if unknown.
	 * @return The config value.
	 */
	public static String getConfigString(Property prop, boolean client){
		if(prop.getType() != Property.Type.STRING || prop.isList()){
			throw new ClassCastException(Essentials.MODID + ": Incorrect config property type.");
		}
		//The NBT should never be null, but just to be safe, it is better to have the wrong client config than crash.
		if(!client || syncPropNBT == null){
			return prop.getString();
		}
		int index = SYNCED_PROPERTIES.indexOf(prop);
		if(index == -1){
			return prop.getString();
		}
		return syncPropNBT.getString("p_" + index);
	}

	/**
	 * Returns the value of the Property, but if client side it will return the server side value if the Property is in the list SYNCED_PROPERTIES.
	 * 
	 * @param prop The property to get the value of.
	 * @param client Whether this is on the virtual client. Pass this param. true if unknown.
	 * @return The config value.
	 */
	public static String[] getConfigStringList(Property prop, boolean client){
		if(prop.getType() != Property.Type.STRING || !prop.isList()){
			throw new ClassCastException(Essentials.MODID + ": Incorrect config property type.");
		}
		//The NBT should never be null, but just to be safe, it is better to have the wrong client config than crash.
		if(!client || syncPropNBT == null){
			return prop.getStringList();
		}
		int index = SYNCED_PROPERTIES.indexOf(prop);
		if(index == -1){
			return prop.getStringList();
		}
		String[] out = new String[syncPropNBT.getInteger("p_" + index)];
		for(int i = 0; i < out.length; i++){
			out[i] = syncPropNBT.getString("p_" + index + "_" + i);
		}
		return out;
	}
	
	public static NBTTagCompound nbtToSyncConfig(){
		NBTTagCompound out = new NBTTagCompound();
		int i = 0;
		for(Property prop : SYNCED_PROPERTIES){
			switch(prop.getType()){
				case BOOLEAN:
					if(!prop.isList()){
						out.setBoolean("p_" + i, prop.getBoolean());
					}else{
						//Not supported
					}
					break;
				case COLOR:
					//Not supported
					break;
				case DOUBLE:
					if(!prop.isList()){
						out.setDouble("p_" + i, prop.getDouble());
					}else{
						//Not supported
					}
					break;
				case INTEGER:
					if(!prop.isList()){
						out.setInteger("p_" + i, prop.getInt());
					}else{
						//Not supported
					}
					break;
				case MOD_ID:
					//Not supported
					break;
				case STRING:
					if(!prop.isList()){
						out.setBoolean("p_" + i, prop.getBoolean());
					}else{
						out.setInteger("p_" + i, prop.getStringList().length);
						for(int ind = 0; ind < prop.getStringList().length; ind++){
							out.setString("p_" + i + "_" + ind, prop.getStringList()[ind]);
						}
					}
					break;
				default:
					break;
			}
			i++;
		}
		return out;
	}
	
	/**
	 * @param stack The stack to test
	 * @param client Whether this is on the client side
	 * @return Whether this item is considered a wrench
	 */
	public static boolean isWrench(ItemStack stack, boolean client){
		if(stack.isEmpty()){
			return false;
		}
		ResourceLocation loc = stack.getItem().getRegistryName();
		if(loc == null){
			return false;
		}
		String name = loc.toString();
		for(String s : getConfigStringList(wrenchTypes, client)){
			if(name.equals(s)){
				return true;
			}
		}
		return false;
	}
}
