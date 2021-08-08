package com.rpc.common.netty.serializer;

public interface SerializerAlgorithm {
    /**
     * json 序列化标识
     */
    byte JSON = 1;
    byte PROTO_BUFF = 2;
}
