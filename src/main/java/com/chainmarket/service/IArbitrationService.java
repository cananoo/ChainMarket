package com.chainmarket.service;

import com.chainmarket.entity.Arbitration;
import java.util.List;

import com.chainmarket.entity.ArbitrationEvidence;
import com.chainmarket.entity.Order;
import org.springframework.web.multipart.MultipartFile;

public interface IArbitrationService {
    
    /**
     * 获取所有待处理的仲裁案件
     */
    List<Arbitration> getPendingArbitrations();

    /**
     * 申请仲裁
     */
    void applyArbitration(Long orderId, Long userId, String disputeDesc, 
                         MultipartFile evidenceFile, String evidenceDesc);

    /**
     * 获取用户的订单列表
     */
    List<Order> getUserOrders(Long userId);

    /**
     * 根据ID获取仲裁案件
     */
    Arbitration getArbitrationById(Long caseId);

    /**
     * 加入仲裁案件
     */
    void joinArbitration(Long caseId, Long userId);

    /**
     * 获取仲裁案件的证据列表
     */
    List<ArbitrationEvidence> getArbitrationEvidences(Long caseId);
} 