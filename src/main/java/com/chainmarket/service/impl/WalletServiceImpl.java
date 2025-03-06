package com.chainmarket.service.impl;

import com.chainmarket.service.IWalletService;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WalletServiceImpl implements IWalletService {
    
    private Map<String, BigDecimal> balanceMap = new ConcurrentHashMap<>();
    
    @Override
    public String generateWalletAddress() {
        // 生成UUID并去掉'-'
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 补充到40位
        while (uuid.length() < 40) {
            uuid += "0";
        }
        // 添加"0x"前缀
        return "0x" + uuid;
    }
    
    @Override
    public BigDecimal getBalance(String address) {
        return balanceMap.getOrDefault(address, BigDecimal.ZERO);
    }
    
    @Override
    public void recharge(String address, BigDecimal amount) {
        balanceMap.compute(address, (key, oldValue) -> {
            if (oldValue == null) {
                return amount;
            }
            return oldValue.add(amount);
        });
    }
    
    // 其他钱包相关操作直接调用FISCO BCOS的API
} 