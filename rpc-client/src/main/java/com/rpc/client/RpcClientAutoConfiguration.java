package com.rpc.client;

import com.rpc.client.core.RpcClient;
import com.rpc.client.discovery.ZKServiceDiscovery;
import com.rpc.common.properties.ZKProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConditionalOnClass(RpcClient.class)
public class RpcClientAutoConfiguration {
    @ConditionalOnMissingBean
    @Bean
    public ZKProperties defaultZKProperties(){
        return new ZKProperties();
    }

    @ConditionalOnMissingBean
    @Bean
    public ZKServiceDiscovery zkServiceDiscovery() {
        return new ZKServiceDiscovery();
    }

    @ConditionalOnMissingBean
    @Bean
    public RpcClient rpcClient() {
        return new RpcClient();
    }
}
