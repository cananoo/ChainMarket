package com.chainmarket.util;

import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class BcosClientWrapper {
    
    private static final String CONFIG_FILE = "src/main/resources/config-example.toml";
    private static final String ABI_PATH = "src/main/resources/abi/";
    private static final String BINARY_PATH = "";
    private static final int GROUP_ID = 1;
    
    private BcosSDK sdk;
    private Client client;
    private CryptoKeyPair keyPair;
    private AssembleTransactionProcessor transactionProcessor;

    @PostConstruct
    public void init() {
        // 初始化BcosSDK
        sdk = BcosSDK.build(CONFIG_FILE);
        
        // 获取Client对象
        client = sdk.getClient(GROUP_ID);
        
        // 生成密钥对
        keyPair = client.getCryptoSuite().createKeyPair();
        
        // 构造交易处理器
        try {
            transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(
                client, 
                keyPair,
                ABI_PATH,
                BINARY_PATH
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize transaction processor", e);
        }
    }

    /**
     * 获取交易处理器实例
     */
    public AssembleTransactionProcessor getTransactionProcessor() {
        return transactionProcessor;
    }

    /**
     * 获取客户端实例
     */
    public Client getClient() {
        return client;
    }

    /**
     * 获取密钥对
     */
    public CryptoKeyPair getKeyPair() {
        return keyPair;
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.stop();
        }
        if (sdk != null) {
            sdk.stopAll();
        }
    }
} 