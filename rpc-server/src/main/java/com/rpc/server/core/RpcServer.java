package com.rpc.server.core;

import com.rpc.common.netty.packet.Spliter;
import com.rpc.common.util.ServiceUtil;
import com.rpc.common.util.ThreadPoolUtil;
import com.rpc.server.annotation.RpcService;
import com.rpc.server.handler.IMIdleStateHandler;
import com.rpc.server.handler.PacketCodecHandler;
import com.rpc.server.handler.RpcServerHandler;
import com.rpc.server.properties.RpcServerProperties;
import com.rpc.server.registry.ZKServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private Map<String, Object> serviceMap = new HashMap<>();
    private Thread thread;
//    private ServiceRegistry serviceRegistry;

//    @Value("${rpc.server.address}")
//    private String serverAddress;

    @Autowired
    private ZKServiceRegistry ZKServiceRegistry;

    @Autowired
    private RpcServerProperties rpcServerProperties;

    /**
     * 在类初始化时执行，将所有被 @RpcService 标记的类纳入管理
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> rpcServiceMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (!CollectionUtils.isEmpty(rpcServiceMap)) {
            for (Object object : rpcServiceMap.values()) {
                RpcService rpcService = object.getClass().getAnnotation(RpcService.class);
                String serviceName = rpcService.value().getName();
                System.out.println("存入服务: " + serviceName);
                serviceMap.put(serviceName, object);
            }
        }
//        for (Object serviceBean:beans.values()) {
//            Class<?> clazz = serviceBean.getClass();
//            Class<?>[] interfaces = clazz.getInterfaces();
//
//            for (Class<?> inter : interfaces) {
//                String interfaceName = inter.getName();
//                serviceMap.put(interfaceName, serviceBean);
//            }
//        }
        System.out.println("已加载全部服务接口");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void addService(String interfaceName, String version, Object serviceBean) {
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
        System.out.println("serviceKey: " + serviceKey + " serviceBean: " + serviceBean);
    }

    public void start() {
        thread = new Thread(new Runnable() {
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(
                    RpcServer.class.getSimpleName(), 16, 32);
            @Override
            public void run() {
                final EventLoopGroup bossGroup = new NioEventLoopGroup();
                final EventLoopGroup workerGroup = new NioEventLoopGroup();
                try{
                    final ServerBootstrap serverBootstrap = new ServerBootstrap();
                    serverBootstrap
                            .group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 1024)
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childHandler(new ChannelInitializer<NioSocketChannel>() {
                                @Override
                                protected void initChannel(NioSocketChannel socketChannel){
                                    socketChannel.pipeline().addLast(new IMIdleStateHandler());
                                    socketChannel.pipeline().addLast(new Spliter());
                                    socketChannel.pipeline().addLast(PacketCodecHandler.INSTANCE);
                                    socketChannel.pipeline().addLast(new RpcServerHandler(serviceMap, threadPoolExecutor));
                                }
                            });
                    ChannelFuture future = serverBootstrap.bind(rpcServerProperties.getPort()).sync();
                    System.out.println("in server");
                    System.out.println("server started,  " + rpcServerProperties.getPort());
                    String serviceAddress = InetAddress.getLocalHost().getHostAddress() + ":" + rpcServerProperties.getPort();
                    System.out.println(serviceMap);
                    for (String interfaceName : serviceMap.keySet()) {
                        ZKServiceRegistry.registry(interfaceName, serviceAddress);
                        System.out.println("registry service: " + interfaceName + "=>"  + serviceAddress);
                    }
//                    String[] array = serverAddress.split(":");
//                    String host = array[0];
//                    int port = Integer.parseInt(array[1]);
//                    ChannelFuture cf = serverBootstrap.bind(host, port).sync();
//                    registry.registry(serverAddress);
                    // 等待服务端监听端口关闭
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            }

        });
        thread.start();
    }
}
