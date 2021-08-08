package com.rpc.common.config;

public interface Constant {
    int ZK_SESSION_TIMOUT = 5000;
    int ZK_CONNECTION_TIMOUT = 5000;

    String ZK_REGISTRY_PATH = "/registry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

    String ZK_NAMESPACE = "netty-rpc";
}
