package com.rpc.rpcprovider.Impl;
import com.rpc.rpclib.HelloService;
import com.rpc.server.annotation.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String name) {
        return "Hello" + name;
    }
}
