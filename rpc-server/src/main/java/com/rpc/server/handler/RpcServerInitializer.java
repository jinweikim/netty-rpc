package com.rpc.server.handler;

import com.rpc.common.netty.packet.Spliter;
import com.rpc.common.netty.serializer.Serializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private Map<String, Object> handlerMap;
    private ThreadPoolExecutor threadPoolExecutor;

    public RpcServerInitializer(Map<String, Object> handlerMap, ThreadPoolExecutor threadPoolExecutor) {
        this.handlerMap = handlerMap;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline cp = channel.pipeline();
        cp.addLast(new IMIdleStateHandler());
        cp.addLast(new Spliter());
        cp.addLast(PacketCodecHandler.INSTANCE);
        cp.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));
    }
}
