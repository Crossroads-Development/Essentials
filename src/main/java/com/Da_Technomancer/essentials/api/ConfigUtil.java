package com.Da_Technomancer.essentials.api;

import com.Da_Technomancer.essentials.ESConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;

import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ConfigUtil{
	/**
	 * A common style applied to "quip" lines in tooltips
	 */
	public static final Style TT_QUIP = Style.EMPTY.applyFormat(ChatFormatting.AQUA).withItalic(true);

	public static final ItemAbility WRENCH_ACTION = ItemAbility.get("wrench");//No single standard for wrench tool action name has emerged yet
	private static final TagKey<Item> WRENCH = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "tools/wrench"));

	/**
	 * @param stack The stack to test
	 * @return Whether this item is considered a wrench
	 */
	public static boolean isWrench(ItemStack stack){
		//Essentials prefers wrenches defined via the forge:item/wrench.json, but will also check tool actions- which some mods use to define their wrench
		return stack.is(WRENCH) || stack.canPerformAction(WRENCH_ACTION);
	}

	private static final NumberFormat PLAIN = new DecimalFormat("0.000");
	private static final NumberFormat SCIENTIFIC = new DecimalFormat("0.000E0");
	private static final NumberFormat ENGINEERING = new DecimalFormat("##0.000E0");

	/**
	 * Formats floating point values for display
	 * @param value The value to format
	 * @param format The format to conform the value to.
	 * @return The formatted string version, for display
	 */
	public static String formatFloat(float value, @Nonnull NumberTypes format){
		if(format == NumberTypes.HEX){
			//This option exists mainly for debugging. It shows the entire hex definition of the float value
			return Float.toHexString(value);
		}

		float absValue = Math.abs(value);
		if(absValue == 0){
			return "0";
		}
		if(absValue >= 10_000 || absValue < 0.001F){
			if(format == NumberTypes.SCIENTIFIC){
				return SCIENTIFIC.format(value);
			}else if(format == NumberTypes.ENGINEERING){
				return ENGINEERING.format(value);
			}
		}
		return PLAIN.format(value);
	}

	/**
	 * Formats floating point values for display
	 * @param value The value to format
	 * @param format The format to conform the value to.
	 * @return The formatted string version, for display
	 */
	public static String formatDouble(double value, @Nonnull NumberTypes format){
		if(format == NumberTypes.HEX){
			//This option exists mainly for debugging. It shows the entire hex definition of the double value
			return Double.toHexString(value);
		}

		double absValue = Math.abs(value);
		if(absValue == 0){
			return "0";
		}
		if(absValue >= 10_000 || absValue < 0.001F){
			if(format == NumberTypes.SCIENTIFIC){
				return SCIENTIFIC.format(value);
			}else if(format == NumberTypes.ENGINEERING){
				return ENGINEERING.format(value);
			}
		}
		return PLAIN.format(value);
	}

	private static final DecimalFormat INTEGER_PLAIN = new DecimalFormat("0");
	private static final DecimalFormat INTEGER_SCIENTIFIC = new DecimalFormat("0.000E0");
	private static final DecimalFormat INTEGER_ENGINEERING = new DecimalFormat("##0.000E0");

	/**
	 * Formats integer values for display
	 * @param i The value to format
	 * @param format The format to conform the value to.
	 * @return The formatted string version, for display
	 */
	public static String formatInteger(int i, @Nonnull NumberTypes format){
		if(format == ConfigUtil.NumberTypes.HEX){
			return Integer.toHexString(i);
		}
		final int absValue = Math.abs(i);
		switch(format){
			case SCIENTIFIC:
				if(absValue >= 10000){
					return INTEGER_SCIENTIFIC.format(i);
				}
				break;
			case ENGINEERING:
				if(absValue >= 10000){
					return INTEGER_ENGINEERING.format(i);
				}
				break;
		}

		return INTEGER_PLAIN.format(i);
	}

	/**
	 * Virtual client-side only - may crash otherwise
	 * @param d Value to be formatted for display
	 * @return String representing the number
	 */
	public static String formatNumberClient(double d){
		return formatDouble(d, ESConfig.numberDisplay.get());
	}

	/**
	 * Virtual client-side only - may crash otherwise
	 * @param f Value to be formatted for display
	 * @return String representing the number
	 */
	public static String formatNumberClient(float f){
		return formatFloat(f, ESConfig.numberDisplay.get());
	}

	/**
	 * Virtual client-side only - may crash otherwise
	 * @param i Value to be formatted for display
	 * @return String representing the number
	 */
	public static String formatNumberClient(int i){
		return formatInteger(i, ESConfig.numberDisplay.get());
	}

	public enum NumberTypes{

		NORMAL(),
		SCIENTIFIC(),//Scientific notation when magnitude outside of 0.001-10000
		ENGINEERING(),//Engineering notation when magnitude outside of 0.001-10000
		HEX()//Display the raw float hexadecimal. This exists mainly for debugging. You want this? WHAT IS WRONG WITH YOU?
	}
}
