package com.rpc.common.netty.packet;

import com.rpc.common.entity.RequestPacket;
import com.rpc.common.entity.ResponsePacket;
import com.rpc.common.netty.serializer.JSONSerializer;
import com.rpc.common.netty.serializer.ProtoBuffSerializer;
import com.rpc.common.netty.serializer.Serializer;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.rpc.common.entity.Command.REQUEST;
import static com.rpc.common.entity.Command.RESPONSE;


// 通信协议中的魔数

public class PacketCodeC {

    public static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte, Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer> serializerMap;
    public static final PacketCodeC INSTANCE = new PacketCodeC();

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(REQUEST, RequestPacket.class);
        packetTypeMap.put(RESPONSE, ResponsePacket.class);

        serializerMap = new HashMap<>();
        Serializer serializerJSON = new JSONSerializer();
        Serializer serializerProtoBuff = new ProtoBuffSerializer();
        serializerMap.put(serializerJSON.getSerializerAlgorithm(), serializerJSON);
        serializerMap.put(serializerProtoBuff.getSerializerAlgorithm(), serializerProtoBuff);
    }

    public ByteBuf encode(ByteBuf byteBuf, Packet packet) {
        //  调用默认序列化算法
        byte[] bytes = Serializer.DEFAULT.serialize(packet);
        // 实际编码过程
        // 魔数
        byteBuf.writeInt(MAGIC_NUMBER);
        // 协议版本号
        byteBuf.writeByte(packet.getVersion());
        // 序列化算法
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        // 指令类型
        byteBuf.writeByte(packet.getCommand());
        // 数据长度
        byteBuf.writeInt(bytes.length);
        // 数据
        byteBuf.writeBytes(bytes);

        return byteBuf;

    }

    public Packet decode(ByteBuf byteBuf) {
        // 跳过魔数
        byteBuf.skipBytes(4);

        // 跳过版本号
        byteBuf.skipBytes(1);

        // 获取序列化算法标识
        byte serializeAlgorithm = byteBuf.readByte();

        // 获取指令类型
        byte command = byteBuf.readByte();

        // 获取数据包长度
        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializeAlgorithm);

        if (requestType != null && serializer != null) {
            return serializer.deserialize(requestType, bytes);
        }

        return null;
    }

    public Serializer getSerializer(byte serializeAlgorithm) {
        return serializerMap.get(serializeAlgorithm);
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }


}
