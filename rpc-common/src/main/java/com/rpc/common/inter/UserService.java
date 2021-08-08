package com.rpc.common.inter;

import com.rpc.common.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> insertUser(User user);
    User getUserById(String id);
    void deleteUserById(String id);
    String getUserNameById(String id);
    Map<String, User> getAllUser();

}
