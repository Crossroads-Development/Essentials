package com.Da_Technomancer.essentials.integration;

import com.Da_Technomancer.essentials.Essentials;

public class PatchouliProxy{

	/*
	 * This class will crash if anything inside is called when Patchouli is not installed, but can be referenced as long as it isn't called
	 */

	public static void initBookItem(){
		ESIntegration.bookItem = new PatchouliBook();
		Essentials.logger.info("Attempted to initialize Patchouli integration, but it has been disabled in code");
		Essentials.logger.info("Notify the mod author that Patchouli integration should be re-enabled");
	}
}
