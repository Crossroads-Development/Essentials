package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class ConfigureWrenchOnServer extends ServerPacket{

	public int modeIndex;

	private static final Field[] FIELDS = fetchFields(ConfigureWrenchOnServer.class, "modeIndex");

	public ConfigureWrenchOnServer(){

	}

	public ConfigureWrenchOnServer(int newModeIndex){
		this.modeIndex = newModeIndex;
	}

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void run(@Nullable ServerPlayerEntity player){
		if(player == null){
			Essentials.logger.warn("Player was null on packet arrival");
			return;
		}
		Hand hand = null;
		if(player.getHeldItemMainhand().getItem() == ESItems.circuitWrench){
			hand = Hand.MAIN_HAND;
		}else if(player.getHeldItemOffhand().getItem() == ESItems.circuitWrench){
			hand = Hand.OFF_HAND;
		}

		if(hand != null){
			ItemStack held = player.getHeldItem(hand);
			held.getOrCreateTag().putInt(CircuitWrench.NBT_KEY, modeIndex);
		}
	}
}
