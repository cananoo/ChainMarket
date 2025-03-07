package com.chainmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chainmarket.dao.ArbitrationDao;
import com.chainmarket.dao.ArbitrationEvidenceDao;
import com.chainmarket.dao.OrderDao;
import com.chainmarket.entity.Arbitration;
import com.chainmarket.entity.ArbitrationEvidence;
import com.chainmarket.entity.Order;
import com.chainmarket.service.IArbitrationService;
import com.chainmarket.service.IImageService;
import com.chainmarket.service.ISystemParamService;
import com.chainmarket.util.SnowflakeIdGenerator;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ArbitrationServiceImpl implements IArbitrationService {
    
    private static final String WALLET_ADDRESS = "0x3a20b086b5523c49ea04c2e16ba1dac63f8b51a1";
    
    @Autowired
    private ArbitrationDao arbitrationDao;
    
    @Autowired
    private ArbitrationEvidenceDao arbitrationEvidenceDao;
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private IImageService imageService;
    
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    
    @Autowired
    private ISystemParamService systemParamService;
    
    @Override
    public List<Arbitration> getPendingArbitrations() {
        return arbitrationDao.selectPendingArbitrations();
    }
    
    @Override
    @Transactional
    public void applyArbitration(Long orderId, Long userId, String disputeDesc, 
                                MultipartFile evidenceFile, String evidenceDesc) {
        // 1. 创建仲裁案件
        Arbitration arbitration = new Arbitration();
        arbitration.setCaseNo(String.valueOf(idGenerator.nextId())); // 生成案件编号
        arbitration.setOrderId(orderId);
        arbitration.setInitiatorId(userId);
        arbitration.setDisputeDesc(disputeDesc);
        arbitration.setStatus(0); // 待处理状态
        
        arbitrationDao.insert(arbitration);
        
        // 2. 上传证据文件
        String evidenceUrl = imageService.uploadImage(evidenceFile);
        
        // 3. 保存证据信息
        ArbitrationEvidence evidence = new ArbitrationEvidence();
        evidence.setCaseId(arbitration.getCaseId());
        evidence.setUserId(userId);
        evidence.setEvidenceUrl(evidenceUrl);
        evidence.setDescription(evidenceDesc);
        
        arbitrationEvidenceDao.insert(evidence);
    }
    
    @Override
    public List<Order> getUserOrders(Long userId) {
        // 获取用户作为买家的订单
        return orderDao.selectUserBuyOrders(userId);
    }

    @Override
    public Arbitration getArbitrationById(Long caseId) {
        return arbitrationDao.selectById(caseId);
    }

    @Override
    @Transactional
    public void joinArbitration(Long caseId, Long userId) {
        // 检查仲裁案件是否存在
        Arbitration arbitration = arbitrationDao.selectById(caseId);
        if (arbitration == null) {
            throw new BusinessException("仲裁案件不存在");
        }
        
        // TODO: 调用智能合约加入仲裁案件
        // 后续实现智能合约调用
        


    }

    @Override
    public List<ArbitrationEvidence> getArbitrationEvidences(Long caseId) {
        return arbitrationEvidenceDao.selectList(
            new QueryWrapper<ArbitrationEvidence>()
                .eq("caseId", caseId)
                .orderByAsc("createTime")
        );
    }
} 