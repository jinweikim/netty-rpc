package com.rpc.client.discovery;

import com.rpc.common.properties.ZKProperties;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@EnableConfigurationProperties(ZKProperties.class)
public class ZKServiceDiscovery {

    @Autowired
    private ZKProperties zkProperties;

    private String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    public String getAddress(List<String> addressList) {
        if (CollectionUtils.isEmpty(addressList)) {
            String defaultAddress="localhost:2181";
            System.out.println("addressList is empty, using defaultAddress" +defaultAddress);
            return defaultAddress;
        }
        String address = getRandomAddress(addressList);
        System.out.println("using address" + address);
        return address;
    }

    public String getRandomAddress(List<String> addressList) {
//        return addressList.get(ThreadLocalRandom.current().nextInt(addressList.size()));
        return DEFAULT_ZOOKEEPER_ADDRESS;
    }


    /**
     * 为客户的提供地址
     * 根据服务名获取服务地址
     */
    public String discovery(String serviceName) {
        ZkClient zkClient = new ZkClient(getAddress(zkProperties.getAddressList()), zkProperties.getSessionTimeout(), zkProperties.getConnectTimeout());
        try{
            String servicePath = zkProperties.getRegistryPath() + "/" + serviceName;
            System.out.println("servicePath: " + servicePath);

            // 找不到对应服务
            if (!zkClient.exists(servicePath)) {
                System.out.println("can not find service");
            }
            List<String> addressList = zkClient.getChildren(servicePath);
            if (CollectionUtils.isEmpty(addressList)) {
                System.out.println("can not find any address");
            }
//            String address = getRandomAddress(addressList);
//            String address = "address-0000000010";
            for (String address : addressList) {
                System.out.println(address);
            }
            String address = addressList.get(0);
            System.out.println("i choose addres: " + address);
            return zkClient.readData(servicePath+"/"+address);
        } finally {
            zkClient.close();
        }
    }
}
