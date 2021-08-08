package com.rpc.common.netty.serializer;

public interface Serializer {

    /**
     * 序列化算法
     */
    byte getSerializerAlgorithm();

    /**
     * java 对象转换成二进制
     */
    <T> byte[] serialize(T object);

    /**
     * 二进制转换成 java 对象
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    /**
     * json 序列化
     */
    byte JSON_SERIALIZER = 1;
//    Serializer DEFAULT = new JSONSerializer();
    Serializer DEFAULT = new ProtoBuffSerializer();
}
