package com.chainmarket.service.impl;

import com.chainmarket.service.IWalletService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class WalletServiceImpl implements IWalletService {
    
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
    
    // 其他钱包相关操作直接调用FISCO BCOS的API
} 