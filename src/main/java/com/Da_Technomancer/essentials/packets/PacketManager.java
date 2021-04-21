package com.Da_Technomancer.essentials.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
		//Primitives and primitive wrappers
		addCodec(boolean.class, (val, buf) -> buf.writeBoolean((boolean) val), PacketBuffer::readBoolean);
		addCodec(Boolean.class, (val, buf) -> buf.writeBoolean((Boolean) val), PacketBuffer::readBoolean);
		addCodec(byte.class, (val, buf) -> buf.writeByte((byte) val), PacketBuffer::readByte);
		addCodec(Byte.class, (val, buf) -> buf.writeByte((Byte) val), PacketBuffer::readByte);
		addCodec(int.class, (val, buf) -> buf.writeInt((int) val), PacketBuffer::readInt);
		addCodec(Integer.class, (val, buf) -> buf.writeInt((Integer) val), PacketBuffer::readInt);
		addCodec(long.class, (val, buf) -> buf.writeLong((long) val), PacketBuffer::readLong);
		addCodec(Long.class, (val, buf) -> buf.writeLong((Long) val), PacketBuffer::readLong);
		addCodec(float.class, (val, buf) -> buf.writeFloat((float) val), PacketBuffer::readFloat);
		addCodec(Float.class, (val, buf) -> buf.writeFloat((Float) val), PacketBuffer::readFloat);
		addCodec(double.class, (val, buf) -> buf.writeDouble((double) val), PacketBuffer::readDouble);
		addCodec(Double.class, (val, buf) -> buf.writeDouble((Double) val), PacketBuffer::readDouble);
		//Other
		addCodec(String.class, (val, buf) -> buf.writeUtf((String) val), PacketManager::readString);
		addCodec(BlockPos.class, (val, buf) -> buf.writeBlockPos((BlockPos) val), PacketBuffer::readBlockPos);
		addCodec(CompoundNBT.class, (val, buf) -> buf.writeNbt((CompoundNBT) val), PacketBuffer::readNbt);
		//Arrays
		addCodec(byte[].class, (val, buf) -> buf.writeByteArray((byte[]) val), PacketBuffer::readByteArray);
	}

	public static void addCodec(Class<?> clazz, BiConsumer<Object, PacketBuffer> writer, Function<PacketBuffer, Object> reader){
		writeMap.put(clazz, writer);
		readMap.put(clazz, reader);
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
					throw new IllegalStateException();
				}
			}
		}
	}

	public static <T extends Packet> T decode(PacketBuffer buf, Class<T> clazz){
		T packet;
		try{
			packet = clazz.getConstructor().newInstance();
		}catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e){
			Essentials.logger.error("Unable to instantiate packet. Report to mod author: " + clazz.toString(), e);
			throw new IllegalStateException();
		}

		Field[] toDecode;

		toDecode = packet.getFields();

		for(Field f : toDecode){
			Function<PacketBuffer, Object> reader = readMap.get(f.getType());
			if(reader == null){
				Essentials.logger.error("Failed to get reader for packet class. Report to mod author: " + f.getType().toString());
				throw new IllegalStateException();
			}else{
				try{
					f.set(packet, reader.apply(buf));
				}catch(IllegalAccessException | ClassCastException e){
					Essentials.logger.error("Failed to decode packet class. Report to mod author: " + f.getType().toString(), e);
					throw new IllegalStateException();
				}
			}
		}

		return packet;
	}

	public static <T extends Packet> void activate(T packet, Supplier<NetworkEvent.Context> context){
		NetworkEvent.Context cont = context.get();
		packet.consume(cont);
		cont.setPacketHandled(true);
	}

	private static String readString(PacketBuffer buf){
		return buf.readUtf(Short.MAX_VALUE);//Re-implementation that isn't client side only
	}
}
