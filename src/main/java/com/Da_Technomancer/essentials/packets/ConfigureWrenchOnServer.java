package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.items.ESItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;

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
	protected void run(@Nullable ServerPlayer player){
		if(player == null){
			Essentials.logger.warn("Player was null on packet arrival");
			return;
		}
		InteractionHand hand = null;
		if(player.getMainHandItem().getItem() == ESItems.circuitWrench){
			hand = InteractionHand.MAIN_HAND;
		}else if(player.getOffhandItem().getItem() == ESItems.circuitWrench){
			hand = InteractionHand.OFF_HAND;
		}

		if(hand != null){
			ItemStack held = player.getItemInHand(hand);
			held.getOrCreateTag().putInt(CircuitWrench.NBT_KEY, modeIndex);
		}
	}
}
