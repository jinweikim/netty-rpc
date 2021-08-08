package com.rpc.server.Impl;

import com.rpc.common.entity.User;
import com.rpc.common.inter.UserService;

import java.util.*;

public class UserServiceImpl implements UserService {

    // 作为数据库，存储用户信息
    Map<String, User> userMap = new HashMap<>();

    public List<User> insertUser(User user) {
        System.out.println("新增用户信息");
        userMap.put(user.getUserId(), user);
        List<User> userList = new ArrayList<>();
        for (Map.Entry<String, User> next : userMap.entrySet()) {
            userList.add(next.getValue());
        }
        return userList;
    }

    public User getUserById(String id) {
        User user = userMap.get(id);
        return user;
    }

    public void deleteUserById(String id) {
        userMap.remove(id);
    }

    public String getUserNameById(String id) {
        return userMap.get(id).getUserName();
    }

    public Map<String, User> getAllUser() {
        return userMap;
    }
}
