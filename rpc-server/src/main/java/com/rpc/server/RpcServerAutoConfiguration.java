package com.rpc.server;

import com.rpc.server.core.RpcServer;
import com.rpc.server.properties.RpcServerProperties;
import com.rpc.common.properties.ZKProperties;
import com.rpc.server.registry.ZKServiceRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RpcServer.class)
public class RpcServerAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public RpcServerProperties defaultRpcServerProperties() {
        return new RpcServerProperties();
    }

    @ConditionalOnMissingBean
    @Bean
    public ZKProperties defaultZKProperties() {
        return new ZKProperties();
    }

    @ConditionalOnMissingBean
    @Bean
    public ZKServiceRegistry defaultServiceRegistry() {
        return new ZKServiceRegistry();
    }

    @ConditionalOnMissingBean
    @Bean
    public RpcServer rpcServer() {
        return new RpcServer();
    }
}
