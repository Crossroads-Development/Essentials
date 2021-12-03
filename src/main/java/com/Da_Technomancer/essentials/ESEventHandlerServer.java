package com.Da_Technomancer.essentials;

import com.Da_Technomancer.essentials.gui.container.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ESEventHandlerServer{

	//Begin registration events

	@SubscribeEvent
	@SuppressWarnings("unused")
	public void registerContainerTypes(RegistryEvent.Register<MenuType<?>> e){
		Essentials.registerConType(ItemShifterContainer::new, "item_shifter", e);
		Essentials.registerConType(FluidShifterContainer::new, "fluid_shifter", e);
		Essentials.registerConType(SlottedChestContainer::new, "slotted_chest", e);
		Essentials.registerConType(CircuitWrenchContainer::new, "circuit_wrench", e);
		Essentials.registerConType(ConstantCircuitContainer::new, "cons_circuit", e);
		Essentials.registerConType(TimerCircuitContainer::new, "timer_circuit", e);
		Essentials.registerConType(AutoCrafterContainer::new, "auto_crafter", e);
		Essentials.registerConType(DelayCircuitContainer::new, "delay_circuit", e);
		Essentials.registerConType(PulseCircuitContainer::new, "pulse_circuit", e);
	}

	//End registration events
}
