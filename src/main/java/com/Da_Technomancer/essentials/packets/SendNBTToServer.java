package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class SendNBTToServer extends Packet{

	public SendNBTToServer(){

	}

	public CompoundNBT nbt;
	public BlockPos pos;

	public SendNBTToServer(CompoundNBT nbt, BlockPos pos){
		this.nbt = nbt;
		this.pos = pos;
	}

	private static final Field[] FIELDS = new Field[2];

	static{
		try{
			FIELDS[0] = SendNBTToServer.class.getDeclaredField("nbt");
			FIELDS[1] = SendNBTToServer.class.getDeclaredField("pos");
		}catch(NoSuchFieldException e){
			Essentials.logger.error("Failure to specify packet: " + SendNBTToServer.class.toString() + "; Report to mod author", e);
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

		context.enqueueWork(() -> {
			TileEntity te = context.getSender().getEntityWorld().getTileEntity(pos);

			if(te instanceof INBTReceiver){
				((INBTReceiver) te).receiveNBT(nbt);
			}
		});
	}
}
