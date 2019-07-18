package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class SendFloatToServer extends Packet{

	public SendFloatToServer(){

	}

	public BlockPos pos;
	public byte id;
	public float val;

	public SendFloatToServer(int id, float val, BlockPos pos){
		this((byte) id, val, pos);
	}

	public SendFloatToServer(byte id, float val, BlockPos pos){
		this.id = id;
		this.val = val;
		this.pos = pos;
	}

	private static final Field[] FIELDS = new Field[3];

	static{
		try{
			FIELDS[0] = SendFloatToServer.class.getDeclaredField("pos");
			FIELDS[1] = SendFloatToServer.class.getDeclaredField("id");
			FIELDS[2] = SendFloatToServer.class.getDeclaredField("val");
		}catch(NoSuchFieldException e){
			Essentials.logger.error("Failure to specify packet: " + SendFloatToServer.class.toString() + "; Report to mod author", e);
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

			if(te instanceof IFloatReceiver){
				((IFloatReceiver) te).receiveFloat(id, val);
			}
		});
	}
}
