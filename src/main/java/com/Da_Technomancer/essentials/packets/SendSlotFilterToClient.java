package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

@SuppressWarnings("serial")
public class SendSlotFilterToClient extends Message<SendSlotFilterToClient>{

	public SendSlotFilterToClient(){
		
	}

	public NBTTagCompound nbt;
	public BlockPos pos;

	public SendSlotFilterToClient(NBTTagCompound nbt, BlockPos pos){
		this.nbt = nbt;
		this.pos = pos;
	}

	@Override
	public IMessage handleMessage(MessageContext context){
		if(context.side != Side.CLIENT){
			Essentials.logger.error("MessageToClient received on wrong side:" + context.side);
			return null;
		}

		Minecraft minecraft = Minecraft.getMinecraft();
			minecraft.addScheduledTask(new Runnable(){
			@Override
			public void run(){
				TileEntity te = minecraft.world.getTileEntity(pos);

				if(te instanceof INBTReceiver){
					((INBTReceiver) te).receiveNBT(nbt);
				}
			}
		});

		return null;
	}
}
