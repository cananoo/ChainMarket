package com.chainmarket.service;

import com.chainmarket.dto.AuditDTO;
import com.chainmarket.entity.User;
import com.chainmarket.entity.Goods;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionBaseException;

import java.util.List;

public interface IAuditService {
    
    /**
     * 获取待审核用户列表
     */
    List<User> getPendingUsers();
    
    /**
     * 审核用户
     */
    void auditUser(AuditDTO auditDTO) throws ABICodecException, TransactionBaseException;
    
    /**
     * 获取待审核商品列表
     */
    List<Goods> getPendingGoods();
    
    /**
     * 审核商品
     */
    void auditGoods(AuditDTO auditDTO);
} 