package com.rpc.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "zk")
public class ZKProperties {
    private List<String> addressList = new ArrayList<>();
    private int sessionTimeout = 5000;
    private int connectTimeout = 1000;
    private String registryPath = "/defaultRegistry";
}
