package com.rpc.common.entity;

import com.rpc.common.netty.packet.Packet;
import lombok.Data;

import static com.rpc.common.entity.Command.RESPONSE;

@Data
public class ResponsePacket extends Packet {

    private String requestId;

    private int code;

    private String error;

    private Object result;

    @Override
    public Byte getCommand() {
        return RESPONSE;
    }
}
