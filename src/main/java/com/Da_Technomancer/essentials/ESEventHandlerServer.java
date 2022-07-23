package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.gui.container.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

public class ESEventHandlerServer{

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Essentials.MODID, value = Dist.DEDICATED_SERVER)
	public static class ESModEventsServer{

		@SubscribeEvent
		@SuppressWarnings("unused")
		public static void registerContainerTypes(RegisterEvent e){
			e.register(ForgeRegistries.Keys.MENU_TYPES, helper -> {
				ESEventHandlerCommon.ESModEventsCommon.registerConType(ItemShifterContainer::new, "item_shifter", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(FluidShifterContainer::new, "fluid_shifter", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(SlottedChestContainer::new, "slotted_chest", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(CircuitWrenchContainer::new, "circuit_wrench", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(ConstantCircuitContainer::new, "cons_circuit", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(TimerCircuitContainer::new, "timer_circuit", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(AutoCrafterContainer::new, "auto_crafter", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(DelayCircuitContainer::new, "delay_circuit", helper);
				ESEventHandlerCommon.ESModEventsCommon.registerConType(PulseCircuitContainer::new, "pulse_circuit", helper);
			});
		}
	}
}
