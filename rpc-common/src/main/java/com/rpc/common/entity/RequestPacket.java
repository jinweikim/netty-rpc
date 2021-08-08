package com.rpc.common.entity;

import com.rpc.common.netty.packet.Packet;
import lombok.Data;

import static com.rpc.common.entity.Command.REQUEST;

@Data
public class RequestPacket extends Packet {
    private String requestId;

    private String interfaceName; // 类名

    private String methodName; // 函数名称

    private Class<?>[] parameterTypes; // 参数类型

    private Object[] parameters; // 参数列表

    @Override
    public Byte getCommand() {
        return REQUEST;
    }
}
