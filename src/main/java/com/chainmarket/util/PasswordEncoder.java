package com.chainmarket.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密工具类
 */
public class PasswordEncoder {
    
    /**
     * MD5加密
     */
    public static String encode(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
    
    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
} 