package com.rpc.client.core;

import com.rpc.client.discovery.ZKServiceDiscovery;
import com.rpc.client.handler.RpcClientHandler;
import com.rpc.common.entity.RequestPacket;
import com.rpc.common.entity.ResponsePacket;
import com.rpc.common.netty.packet.PacketCodeC;
import com.rpc.common.netty.packet.PacketDecoder;
import com.rpc.common.netty.packet.PacketEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@AutoConfigureAfter(ZKServiceDiscovery.class)
public class RpcClient {
    @Autowired
    private ZKServiceDiscovery zkServiceDiscovery;

    private ConcurrentMap<String, ResponsePacket> responseMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象
                        RequestPacket requestPacket = new RequestPacket();
                        requestPacket.setRequestId(UUID.randomUUID().toString());
                        requestPacket.setInterfaceName(method.getDeclaringClass().getName());
                        requestPacket.setMethodName(method.getName());
                        requestPacket.setParameterTypes(method.getParameterTypes());
                        requestPacket.setParameters(args);

                        // 获取 RPC 服务地址
                        String serviceName = interfaceClass.getName();
                        String serviceAddress = zkServiceDiscovery.discovery(serviceName);
                        System.out.println("get serviceAddress: " + serviceAddress);

                        // 从 RPC 服务地址中解析主机名与端口号
                        String[] stringArray = StringUtils.split(serviceAddress,":");
                        String host = Objects.requireNonNull(stringArray)[0];
                        int port = Integer.parseInt(stringArray[1]);

                        // 发送 RPC 请求
                        ResponsePacket responsePacket = send(requestPacket, host, port);

                        if (responsePacket == null) {
                            System.out.println("send request failure");
                            return null;
                        }
                        if (responsePacket.getError() != null) {
                            System.out.println("response has exception" + responsePacket.getError());
                            return null;
                        }
                        return responsePacket.getResult();
                    }
                });
    }

    private ResponsePacket send(RequestPacket requestPacket, String host, int port) {
        System.out.println("send begin: " + host + ": " + port);
        EventLoopGroup group = new NioEventLoopGroup(1);

        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new PacketEncoder());
                    pipeline.addLast(new PacketDecoder());
                    pipeline.addLast(new RpcClientHandler(responseMap));
                }
            });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            System.out.println("requestId: " + requestPacket.getRequestId());
            Channel channel = future.channel();
            channel.writeAndFlush(requestPacket).sync();
            channel.closeFuture().sync();
            System.out.println("send end");
            return responseMap.get(requestPacket.getRequestId());
        } catch (Exception e) {
            System.out.println("client exception" + e);
            return null;
        } finally {
            group.shutdownGracefully();
            responseMap.remove(requestPacket.getRequestId());
        }
    }

}
