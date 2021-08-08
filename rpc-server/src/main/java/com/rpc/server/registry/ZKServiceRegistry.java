package com.rpc.server.registry;

import com.rpc.common.properties.ZKProperties;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@EnableConfigurationProperties(ZKProperties.class)
public class ZKServiceRegistry {

//    @Value("${registry.address}")
//    private String registryAddress;

    @Autowired
    private ZKProperties zkProperties;

    private ZkClient zkClient;

    @PostConstruct
    public void init() {
        // 创建 ZooKeeper 客户端
        zkClient = new ZkClient(getAddress(zkProperties.getAddressList()), zkProperties.getSessionTimeout(), zkProperties.getConnectTimeout());
        System.out.println("connect to zookeeper");
    }

    public String getAddress(List<String> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            String defaultAddress = "localhost:2181";
            System.out.println("addressList is empty, using defaultAddress" + defaultAddress);
            return defaultAddress;
        }

        String address = getRandomAddress(addressList);
        System.out.println("using address" + address);
        return address;
    }

    public String getRandomAddress(List<String> addressList) {
        return addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));
    }

//    private static final String ZK_REGISTRY_PATH = "/rpc";

    /**
     * 为服务提供注册
     * 将服务地址注册到对应服务名下
     * 断开连接后地址自动清除
     * @param serviceName
     * @param serviceAddress
     */
    public void registry(String serviceName, String serviceAddress) {
        // 创建 registry 节点 (永久)
        String registryPath = zkProperties.getRegistryPath();
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            System.out.println("create registry node: " + registryPath);
        }

        // 创建 service 节点 (永久）
        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            System.out.println("create service node: " + servicePath);
        }

        // 创建 address 节点（临时）
        String addressPath = servicePath + "/address-";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
        System.out.println("service address: " + serviceAddress);
        System.out.println("create address node: " + addressNode);

    }
//    public void registry(String data) {
//        if (data != null) {
//            ZkClient client = connectServer();
//            if (client != null) {
//                AddRootNode(client);
//                createNode(client, data);
//            }
//        }
//    }
//
//    private ZkClient connectServer() {
//        ZkClient client = new ZkClient(registryAddress, 20000, 20000);
//        return client;
//    }

//    private void AddRootNode(ZkClient client) {
//        boolean exits = client.exists(ZK_REGISTRY_PATH);
//        if (!exits) {
//            client.createPersistent(ZK_REGISTRY_PATH);
//            System.out.println("创建 zookeeper 主节点");
//        }
//    }
//
//    private void createNode(ZkClient client, String data) {
//        String path = client.create(ZK_REGISTRY_PATH + "/provider", data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
//        System.out.println("创建 zookeeper 数据节点"+ path + " " + data);
//    }
}
