package com.chainmarket.service;

import com.chainmarket.entity.User;
import com.chainmarket.dto.LoginDTO;

/**
 * 用户服务接口
 */
public interface IUserService {
    
    /**
     * 发送验证码
     * @param phone 手机号
     * @return 验证码
     */
    String sendVerifyCode(String phone);
    
    /**
     * 用户注册
     * @param user 用户信息
     * @param verifyCode 验证码
     */
    void register(User user, String verifyCode);
    
    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 用户信息
     */
    User login(LoginDTO loginDTO);
    
    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);
} 