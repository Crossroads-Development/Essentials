package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class SendSlotFilterToClient extends Packet{

	public SendSlotFilterToClient(){
		
	}

	public NBTTagCompound nbt;
	public BlockPos pos;

	public SendSlotFilterToClient(NBTTagCompound nbt, BlockPos pos){
		this.nbt = nbt;
		this.pos = pos;
	}

	private static final Field[] FIELDS = new Field[2];

	static{
		try{
			FIELDS[0] = SendSlotFilterToClient.class.getDeclaredField("nbt");
			FIELDS[1] = SendSlotFilterToClient.class.getDeclaredField("pos");
		}catch(NoSuchFieldException e){
			Essentials.logger.error("Failure to specify packet: " + SendSlotFilterToClient.class.toString() + "; Report to mod author", e);
		}
	}

	@Nonnull
	@Override
	protected Field[] getFields(){
		return FIELDS;
	}

	@Override
	protected void consume(NetworkEvent.Context context){
		if(context.getDirection() != NetworkDirection.PLAY_TO_CLIENT){
			Essentials.logger.error("Packet " + toString() + " received on wrong side:" + context.getDirection());
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		minecraft.addScheduledTask(() -> {
			TileEntity te = minecraft.world.getTileEntity(pos);

			if(te instanceof INBTReceiver){
				((INBTReceiver) te).receiveNBT(nbt);
			}
		});
	}
}
