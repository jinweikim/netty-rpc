package com.rpc.client.handler;

import com.rpc.common.entity.ResponsePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentMap;

public class RpcClientHandler extends SimpleChannelInboundHandler<ResponsePacket> {

    private ConcurrentMap<String, ResponsePacket> responseMap;

    public RpcClientHandler(ConcurrentMap<String, ResponsePacket> responseMap) {
        this.responseMap = responseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponsePacket responsePacket) throws Exception {
        System.out.println("received response, requestId is " + responsePacket.getRequestId());
        responseMap.put(responsePacket.getRequestId(), responsePacket);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("client caught exception" + cause);
        ctx.close();
    }
}
