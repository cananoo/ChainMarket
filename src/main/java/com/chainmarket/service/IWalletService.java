package com.chainmarket.service;

import java.math.BigDecimal;

public interface IWalletService {
    /**
     * 生成钱包地址
     * @return 钱包地址 (0x开头的42位16进制字符串)
     */
    String generateWalletAddress();

    /**
     * 获取钱包余额
     * @param address 钱包地址
     * @return 余额
     */
    BigDecimal getBalance(String address);

    /**
     * 充值（模拟）
     * @param address 钱包地址
     * @param amount 充值金额
     */
    void recharge(String address, BigDecimal amount);
} 