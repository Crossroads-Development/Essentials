package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketManager{

	private static final HashMap<Class<?>, BiConsumer<Object, PacketBuffer>> writeMap = new HashMap<>();
	private static final HashMap<Class<?>, Function<PacketBuffer, Object>> readMap = new HashMap<>();

	static{
		writeMap.put(Boolean.class, (val, buf) -> buf.writeBoolean((Boolean) val));
		writeMap.put(Byte.class, (val, buf) -> buf.writeByte((Byte) val));
		writeMap.put(Integer.class, (val, buf) -> buf.writeInt((Integer) val));
		writeMap.put(Long.class, (val, buf) -> buf.writeLong((Long) val));
		writeMap.put(Float.class, (val, buf) -> buf.writeFloat((Float) val));
		writeMap.put(Double.class, (val, buf) -> buf.writeDouble((Double) val));
		writeMap.put(BlockPos.class, (val, buf) -> buf.writeBlockPos((BlockPos) val));
		writeMap.put(NBTTagCompound.class, (val, buf) -> buf.writeCompoundTag((NBTTagCompound) val));

		readMap.put(Boolean.class, PacketBuffer::readBoolean);
		readMap.put(Byte.class, PacketBuffer::readByte);
		readMap.put(Integer.class, PacketBuffer::readInt);
		readMap.put(Long.class, PacketBuffer::readLong);
		readMap.put(Float.class, PacketBuffer::readFloat);
		readMap.put(Double.class, PacketBuffer::readDouble);
		readMap.put(BlockPos.class, PacketBuffer::readBlockPos);
		readMap.put(NBTTagCompound.class, PacketBuffer::readCompoundTag);
	}

	public static <T extends Packet> void encode(T packet, PacketBuffer buf){
		Field[] toEncode;

		toEncode = packet.getFields();

		for(Field f : toEncode){
			BiConsumer<Object, PacketBuffer> writer = writeMap.get(f.getType());
			if(writer == null){
				Essentials.logger.error("Failed to get writer for packet class. Report to mod author: " + f.getType().toString());
			}else{
				try{
					writer.accept(f.get(packet), buf);
				}catch(IllegalAccessException | ClassCastException e){
					Essentials.logger.error("Failed to encode packet class. Report to mod author: " + f.getType().toString(), e);
				}
			}
		}
	}

	public static <T extends Packet> T decode(PacketBuffer buf, Class<T> clazz){
		T packet;
		try{
			packet = clazz.newInstance();
		}catch(InstantiationException | IllegalAccessException e){
			Essentials.logger.error("Unable to instantiate packet. Report to mod author: " + clazz.toString(), e);
			return null;
		}

		Field[] toDecode;

		toDecode = packet.getFields();

		for(Field f : toDecode){
			Function<PacketBuffer, Object> reader = readMap.get(f.getType());
			if(reader == null){
				Essentials.logger.error("Failed to get reader for packet class. Report to mod author: " + f.getType().toString());
			}else{
				try{
					f.set(packet, reader.apply(buf));
				}catch(IllegalAccessException | ClassCastException e){
					Essentials.logger.error("Failed to decode packet class. Report to mod author: " + f.getType().toString(), e);
				}
			}
		}

		return packet;
	}

	public static <T extends Packet> void activate(T packet, Supplier<NetworkEvent.Context> context){
		packet.consume(context.get());
	}
}
