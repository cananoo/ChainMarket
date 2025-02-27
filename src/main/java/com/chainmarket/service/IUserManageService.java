package com.chainmarket.service;

import com.chainmarket.entity.User;
import java.util.List;

public interface IUserManageService {
    
    /**
     * 搜索用户
     */
    List<User> searchUsers(String username, Integer roleType);
    
    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, Integer status, String comment);
} 