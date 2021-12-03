package com.Da_Technomancer.essentials.integration;

public class PatchouliProxy{

	/*
	 * This class will crash if anything inside is called when Patchouli is not installed, but can be referenced as long as it isn't called
	 */

	public static void initBookItem(){
		ESIntegration.bookItem = new PatchouliBook();
	}
}
