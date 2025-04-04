package com.chainmarket.service.impl;

import com.chainmarket.service.IWalletService;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import com.chainmarket.util.BcosClientWrapper;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;

@Service
public class WalletServiceImpl implements IWalletService {

    @Autowired
    private BcosClientWrapper bcosClientWrapper;

    private static final String WALLET_ADDRESS = "0x3a20b086b5523c49ea04c2e16ba1dac63f8b51a1";



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
        try {
            AssembleTransactionProcessor processor = bcosClientWrapper.getTransactionProcessor();

            List<Object> params = new ArrayList<>();
            params.add(address);

            TransactionResponse response = processor.sendTransactionAndGetResponseByContractLoader(
                    "Wallet",
                    WALLET_ADDRESS,
                    "getBalance",
                    params
            );

            // 检查是否有错误消息
            if ("Success".equals(response.getReceiptMessages())) {
                String values = response.getValues();
                if (values != null && !values.isEmpty()) {
                    values = values.replaceAll("[\\[\\]\"]", "");
                    return new BigDecimal(values);
                }
                throw new BusinessException("查询余额失败: 返回值为空");
            } else {
                throw new BusinessException("查询余额失败: " + response.getReceiptMessages());
            }
        } catch (Exception e) {
            throw new BusinessException("查询余额失败: " + e.getMessage());
        }
    }

    @Override
    public void recharge(String address, BigDecimal amount) {
        try {
            AssembleTransactionProcessor processor = bcosClientWrapper.getTransactionProcessor();

            List<Object> params = new ArrayList<>();
            params.add(address);
            params.add(amount.toString());

            TransactionResponse response = processor.sendTransactionAndGetResponseByContractLoader(
                    "Wallet",
                    WALLET_ADDRESS,
                    "deposit",
                    params
            );

            if (!"Success".equals(response.getReceiptMessages())) {
                throw new BusinessException("充值失败: " + response.getReceiptMessages());
            }
        } catch (Exception e) {
            throw new BusinessException("充值失败: " + e.getMessage());
        }
    }

    @Override
    public void withdraw(String address, BigDecimal amount) {
        try {
            AssembleTransactionProcessor processor = bcosClientWrapper.getTransactionProcessor();

            List<Object> params = new ArrayList<>();
            params.add(address);  // 添加地址参数
            params.add(amount.toString());

            TransactionResponse response = processor.sendTransactionAndGetResponseByContractLoader(
                    "Wallet",
                    WALLET_ADDRESS,
                    "withdraw",
                    params
            );

            if (!"Success".equals(response.getReceiptMessages())) {
                throw new BusinessException("提现失败: " + response.getReceiptMessages());
            }
        } catch (Exception e) {
            throw new BusinessException("提现失败: " + e.getMessage());
        }
    }

    // 其他钱包相关操作直接调用FISCO BCOS的API
} 