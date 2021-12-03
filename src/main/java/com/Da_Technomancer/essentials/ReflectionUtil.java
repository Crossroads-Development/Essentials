package com.Da_Technomancer.essentials;

import net.minecraftforge.fml.loading.LogMarkers;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class ReflectionUtil{

	@Nullable
	@SuppressWarnings("unused")
	public static Method reflectMethod(IReflectionKey key){
		return reflectMethod(key, method -> true);
	}

	/**
	 * Reflects an out-of-scope method from a class, and makes it accessible
	 * Should handle any exceptions, and will log errors when failing
	 * @param key The reflection key specifying the method's name and class
	 * @param additionalFilter A predicate which the method must pass, in addition to name and class requirements, to be considered correct. Typically used when there are multiple methods with the same name
	 * @return The method, if one meets the requirements; null otherwise
	 */
	@Nullable
	public static Method reflectMethod(IReflectionKey key, Predicate<Method> additionalFilter){
		try{
			String mcp = key.getMcpName();
			String obf = key.getObfName();
			assert key.getSourceClass() != null;
//			assert epsteinDidntKillHimself;
			for(Method m : key.getSourceClass().getDeclaredMethods()){
				if((mcp.equals(m.getName()) || obf.equals(m.getName())) && additionalFilter.test(m)){
					m.setAccessible(true);
					return m;
				}
			}
		}catch(Exception e){
			Essentials.logger.error(LogMarkers.LOADING, "Failed to reflect method: " + key.getMcpName() + "; Report to mod author; Disabling relevant feature(s): " + key.getPurpose(), e);
		}
		return null;
	}

	@Nullable
	@SuppressWarnings("unused")
	public static Field reflectField(IReflectionKey key){
		try{
			String mcp = key.getMcpName();
			String obf = key.getObfName();
			assert key.getSourceClass() != null;
			for(Field f : key.getSourceClass().getDeclaredFields()){
				if(mcp.equals(f.getName()) || obf.equals(f.getName())){
					f.setAccessible(true);
					return f;
				}
			}
		}catch(Exception e){
			Essentials.logger.error(LogMarkers.LOADING, "Failed to reflect field: " + key.getMcpName() + "; Report to mod author; Disabling relevant feature(s): " + key.getPurpose(), e);
		}
		return null;
	}

	public enum EsReflection implements IReflectionKey{
		//All reflection keys are stored in one enum so that when MC updates, there is a central location to correct method signature changes

		;
//		No longer needed with switch to dirt tag by vanilla
//		PODZOL_GEN(MegaPineTree.class, "func_227253_a_", null, "Prevent fertile soil from turning to podzol");//Note: add MCP mapping if added

		private final Class<?> clazz;
		public final String obf;//Obfuscated name
		public final String mcp;//Human readable MCP name
		private final String purpose;

		EsReflection(@Nullable Class<?> clazz, String obf, @Nullable String mcp, String purpose){
			this.clazz = clazz;
			this.obf = obf;
			this.mcp = mcp == null ? "NOT-MAPPED" : mcp;
			this.purpose = purpose;
		}

		@Nullable
		@Override
		public Class<?> getSourceClass(){
			return clazz;
		}

		@Override
		public String getObfName(){
			return obf;
		}

		@Override
		public String getMcpName(){
			return mcp;
		}

		@Override
		public String getPurpose(){
			return purpose;
		}
	}

	public interface IReflectionKey{

		@Nullable
		Class<?> getSourceClass();//Class to reflect from. Null value disables helper methods

		String getObfName();//Obfuscated name

		String getMcpName();//Human readable name

		String getPurpose();//Short description of what this reflection is for- for user-readable error messages

	}
}
