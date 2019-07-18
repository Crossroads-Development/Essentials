package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class ConfigureWrenchOnServer extends Packet{

	public int modeIndex;

	public ConfigureWrenchOnServer(){

	}

	public ConfigureWrenchOnServer(int newModeIndex){
		this.modeIndex = newModeIndex;
	}

	private static final Field[] FIELDS = new Field[1];

	static{
		try{
			FIELDS[0] = ConfigureWrenchOnServer.class.getDeclaredField("modeIndex");
		}catch(NoSuchFieldException e){
			Essentials.logger.error("Failure to specify packet: " + ConfigureWrenchOnServer.class.toString() + "; Report to mod author", e);
		}
	}

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void consume(NetworkEvent.Context context){
		if(context.getDirection() != NetworkDirection.PLAY_TO_SERVER){
			Essentials.logger.error("Packet " + toString() + " received on wrong side:" + context.getDirection());
			return;
		}

		ServerPlayerEntity player = context.getSender();
		if(player == null){
			Essentials.logger.error("Player was null on packet arrival");
			return;
		}
		context.enqueueWork(() -> {
			Hand hand = null;
			if(player.getHeldItemMainhand().getItem() == EssentialsItems.circuitWrench){
				hand = Hand.MAIN_HAND;
			}else if(player.getHeldItemOffhand().getItem() == EssentialsItems.circuitWrench){
				hand = Hand.OFF_HAND;
			}

			if(hand != null){
				ItemStack held = player.getHeldItem(hand);
				held.getOrCreateTag().putInt(CircuitWrench.NBT_KEY, modeIndex);
			}
		});
	}
}
