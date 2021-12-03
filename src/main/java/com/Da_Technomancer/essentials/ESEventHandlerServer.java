package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.gui.container.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ESEventHandlerServer{

	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Essentials.MODID, value = Dist.DEDICATED_SERVER)
	public static class ESModEventsServer{

		@SubscribeEvent
		@SuppressWarnings("unused")
		public static void registerContainerTypes(RegistryEvent.Register<MenuType<?>> e){
			ESEventHandlerCommon.ESModEventsCommon.registerConType(ItemShifterContainer::new, "item_shifter", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(FluidShifterContainer::new, "fluid_shifter", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(SlottedChestContainer::new, "slotted_chest", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(CircuitWrenchContainer::new, "circuit_wrench", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(ConstantCircuitContainer::new, "cons_circuit", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(TimerCircuitContainer::new, "timer_circuit", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(AutoCrafterContainer::new, "auto_crafter", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(DelayCircuitContainer::new, "delay_circuit", e);
			ESEventHandlerCommon.ESModEventsCommon.registerConType(PulseCircuitContainer::new, "pulse_circuit", e);
		}
	}
}
