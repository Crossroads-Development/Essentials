package com.Da_Technomancer.essentials.api.packets;

import com.Da_Technomancer.essentials.Essentials;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

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

	private static final HashMap<Class<?>, BiConsumer<Object, FriendlyByteBuf>> writeMap = new HashMap<>();
	private static final HashMap<Class<?>, Function<FriendlyByteBuf, Object>> readMap = new HashMap<>();

	static{
		//Primitives and primitive wrappers
		addCodec(boolean.class, (val, buf) -> buf.writeBoolean((boolean) val), FriendlyByteBuf::readBoolean);
		addCodec(Boolean.class, (val, buf) -> buf.writeBoolean((Boolean) val), FriendlyByteBuf::readBoolean);
		addCodec(byte.class, (val, buf) -> buf.writeByte((byte) val), FriendlyByteBuf::readByte);
		addCodec(Byte.class, (val, buf) -> buf.writeByte((Byte) val), FriendlyByteBuf::readByte);
		addCodec(int.class, (val, buf) -> buf.writeInt((int) val), FriendlyByteBuf::readInt);
		addCodec(Integer.class, (val, buf) -> buf.writeInt((Integer) val), FriendlyByteBuf::readInt);
		addCodec(long.class, (val, buf) -> buf.writeLong((long) val), FriendlyByteBuf::readLong);
		addCodec(Long.class, (val, buf) -> buf.writeLong((Long) val), FriendlyByteBuf::readLong);
		addCodec(float.class, (val, buf) -> buf.writeFloat((float) val), FriendlyByteBuf::readFloat);
		addCodec(Float.class, (val, buf) -> buf.writeFloat((Float) val), FriendlyByteBuf::readFloat);
		addCodec(double.class, (val, buf) -> buf.writeDouble((double) val), FriendlyByteBuf::readDouble);
		addCodec(Double.class, (val, buf) -> buf.writeDouble((Double) val), FriendlyByteBuf::readDouble);
		//Other
		addCodec(String.class, (val, buf) -> buf.writeUtf((String) val), PacketManager::readString);
		addCodec(BlockPos.class, (val, buf) -> buf.writeBlockPos((BlockPos) val), FriendlyByteBuf::readBlockPos);
		addCodec(CompoundTag.class, (val, buf) -> buf.writeNbt((CompoundTag) val), FriendlyByteBuf::readNbt);
		//Arrays
		addCodec(byte[].class, (val, buf) -> buf.writeByteArray((byte[]) val), FriendlyByteBuf::readByteArray);
	}

	public static void addCodec(Class<?> clazz, BiConsumer<Object, FriendlyByteBuf> writer, Function<FriendlyByteBuf, Object> reader){
		writeMap.put(clazz, writer);
		readMap.put(clazz, reader);
	}

	public static <T extends Packet> void encode(T packet, FriendlyByteBuf buf){
		Field[] toEncode;

		toEncode = packet.getFields();

		for(Field f : toEncode){
			BiConsumer<Object, FriendlyByteBuf> writer = writeMap.get(f.getType());
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

	public static <T extends Packet> T decode(FriendlyByteBuf buf, Class<T> clazz){
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
			Function<FriendlyByteBuf, Object> reader = readMap.get(f.getType());
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

	private static String readString(FriendlyByteBuf buf){
		return buf.readUtf(Short.MAX_VALUE);//Re-implementation that isn't client side only
	}
}
