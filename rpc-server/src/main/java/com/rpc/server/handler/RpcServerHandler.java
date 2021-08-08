package com.rpc.server.handler;

import com.alibaba.fastjson.JSON;
import com.rpc.common.entity.RequestPacket;
import com.rpc.common.entity.ResponsePacket;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class RpcServerHandler extends SimpleChannelInboundHandler<RequestPacket> {


    private final Map<String, Object> serviceMap;
    private final ThreadPoolExecutor serverHandlerPool;

    public RpcServerHandler(Map<String, Object> serviceMap, ThreadPoolExecutor threadPoolExecutor) {
        this.serviceMap = serviceMap;
        this.serverHandlerPool = threadPoolExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestPacket requestPacket) throws Exception {
        if (requestPacket.getRequestId().equals("BEAT")) {
            System.out.println("监听心跳信息");
            return;
        }

        serverHandlerPool.execute(new Runnable() {
            @Override
            public void run() {
                ResponsePacket response = new ResponsePacket();
                response.setRequestId(requestPacket.getRequestId());
                try{
                    Object result = handle(requestPacket);
                    response.setResult(result);
                } catch (Throwable e) {
                    e.printStackTrace();
                    response.setCode(1);
                    response.setError(e.toString());
                }
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        System.out.println("Send response for request " + requestPacket.getRequestId());
                    }
                });
            }
        });
    }

    private Object handle(RequestPacket requestPacket) throws Throwable {
        // 获取服务实例
        String serviceName = requestPacket.getInterfaceName();
        Object serviceBean = serviceMap.get(serviceName);
        System.out.println("service Name" + serviceName);
        System.out.println(serviceBean.toString());

        if (serviceBean != null) {
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = requestPacket.getMethodName();
            System.out.println("调用的方法名: " + methodName);
            Class<?>[] parametersTypes = requestPacket.getParameterTypes();
            Object[] parameters = requestPacket.getParameters();

            // 执行反射调用
            Method method = serviceClass.getMethod(methodName, parametersTypes);
            method.setAccessible(true);
            System.out.println("parameters" + parameters[0].toString());
            System.out.println("执行服务");
            return method.invoke(serviceBean, parameters);
        } else {
            throw new Exception("未找到服务接口，请检查配置");
        }
    }

    private Object[] getParameters(Class<?>[] parameterTypes, Object[] parameters) {
        if (parameters == null || parameters.length == 0) {
            return parameters;
        } else {
            Object[] new_parameters = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                new_parameters[i] = JSON.parseObject(parameters[i].toString(), parameterTypes[i]);
            }
            return new_parameters;
        }
    }
}
