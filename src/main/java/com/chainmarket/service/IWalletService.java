package com.chainmarket.service;

public interface IWalletService {
    /**
     * 生成钱包地址
     * @return 钱包地址 (0x开头的42位16进制字符串)
     */
    String generateWalletAddress();
} 