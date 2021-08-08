package com.rpc.rpcconsumer.controller;

import com.rpc.client.core.RpcClient;
import com.rpc.rpclib.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private RpcClient rpcClient;

    @GetMapping("hello")
    public String sayHello (@RequestParam(defaultValue = "king") String name) {
        HelloService helloService = rpcClient.create(HelloService.class);
        String result = helloService.say(name);
        return result;
    }
}
