package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A magic packet encoder/decoder class inspired by Vazkii's generic Message packet system
 * If a packet extends Packet and reports all fields it needs synced to getFields in constant order, PacketManager will handle encoding/decoding
 */
public class PacketManager{

	private static final HashMap<Class<?>, BiConsumer<Object, PacketBuffer>> writeMap = new HashMap<>();
	private static final HashMap<Class<?>, Function<PacketBuffer, Object>> readMap = new HashMap<>();

	static{
		writeMap.put(boolean.class, (val, buf) -> buf.writeBoolean((boolean) val));
		writeMap.put(Boolean.class, (val, buf) -> buf.writeBoolean((Boolean) val));
		writeMap.put(byte.class, (val, buf) -> buf.writeByte((byte) val));
		writeMap.put(Byte.class, (val, buf) -> buf.writeByte((Byte) val));
		writeMap.put(int.class, (val, buf) -> buf.writeInt((int) val));
		writeMap.put(Integer.class, (val, buf) -> buf.writeInt((Integer) val));
		writeMap.put(long.class, (val, buf) -> buf.writeLong((long) val));
		writeMap.put(Long.class, (val, buf) -> buf.writeLong((Long) val));
		writeMap.put(float.class, (val, buf) -> buf.writeFloat((float) val));
		writeMap.put(Float.class, (val, buf) -> buf.writeFloat((Float) val));
		writeMap.put(double.class, (val, buf) -> buf.writeDouble((double) val));
		writeMap.put(Double.class, (val, buf) -> buf.writeDouble((Double) val));
		writeMap.put(BlockPos.class, (val, buf) -> buf.writeBlockPos((BlockPos) val));
		writeMap.put(CompoundNBT.class, (val, buf) -> buf.writeCompoundTag((CompoundNBT) val));

		readMap.put(boolean.class, PacketBuffer::readBoolean);
		readMap.put(Boolean.class, PacketBuffer::readBoolean);
		readMap.put(byte.class, PacketBuffer::readByte);
		readMap.put(Byte.class, PacketBuffer::readByte);
		readMap.put(int.class, PacketBuffer::readInt);
		readMap.put(Integer.class, PacketBuffer::readInt);
		readMap.put(long.class, PacketBuffer::readLong);
		readMap.put(Long.class, PacketBuffer::readLong);
		readMap.put(float.class, PacketBuffer::readFloat);
		readMap.put(Float.class, PacketBuffer::readFloat);
		readMap.put(double.class, PacketBuffer::readDouble);
		readMap.put(Double.class, PacketBuffer::readDouble);
		readMap.put(BlockPos.class, PacketBuffer::readBlockPos);
		readMap.put(CompoundNBT.class, PacketBuffer::readCompoundTag);
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
