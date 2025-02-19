package com.chainmarket.service.impl;

import com.chainmarket.dao.UserDao;
import com.chainmarket.dao.GoodsDao;
import com.chainmarket.dao.AuditInfoDao;
import com.chainmarket.dto.AuditDTO;
import com.chainmarket.entity.User;
import com.chainmarket.entity.Goods;
import com.chainmarket.entity.AuditInfo;
import com.chainmarket.service.IAuditService;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpSession;
import java.util.List;

@Service
public class AuditServiceImpl implements IAuditService {

    @Autowired
    private UserDao userDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private AuditInfoDao auditInfoDao;
    
    @Autowired
    private HttpSession session;
    
    @Override
    public List<User> getPendingUsers() {
        return userDao.selectPendingUsers();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditUser(AuditDTO auditDTO) {
        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (currentUser.getRoleType() != 9) {
            throw new BusinessException("无权限执行此操作");
        }

        User user = userDao.selectById(auditDTO.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setStatus(auditDTO.getStatus());
        userDao.updateById(user);
        
        // 记录审核信息
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setObjectId(auditDTO.getId());
        auditInfo.setAuditType(1);  // 用户审核
        auditInfo.setAuditStatus(auditDTO.getStatus());
        auditInfo.setAuditOpinion(auditDTO.getComment());
        auditInfo.setAuditorId(currentUser.getUserId());
        auditInfoDao.insert(auditInfo);
    }
    
    @Override
    public List<Goods> getPendingGoods() {
        return goodsDao.selectPendingGoods();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditGoods(AuditDTO auditDTO) {
        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        if (currentUser.getRoleType() != 9) {
            throw new BusinessException("无权限执行此操作");
        }

        Goods goods = goodsDao.selectById(auditDTO.getId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        goods.setStatus(auditDTO.getStatus());
        goodsDao.updateById(goods);
        
        // 记录审核信息
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setObjectId(auditDTO.getId());
        auditInfo.setAuditType(2);  // 商品审核
        auditInfo.setAuditStatus(auditDTO.getStatus());
        auditInfo.setAuditOpinion(auditDTO.getComment());
        auditInfo.setAuditorId(currentUser.getUserId());
        auditInfoDao.insert(auditInfo);
    }
} 