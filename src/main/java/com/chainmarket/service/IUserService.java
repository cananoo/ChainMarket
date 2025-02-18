package com.chainmarket.service;

import com.chainmarket.entity.User;

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
} 