package com.chainmarket.service.impl;

import com.chainmarket.dao.UserDao;
import com.chainmarket.dao.AuditInfoDao;
import com.chainmarket.entity.User;
import com.chainmarket.entity.AuditInfo;
import com.chainmarket.service.IUserManageService;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserManageServiceImpl implements IUserManageService {
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private AuditInfoDao auditInfoDao;
    
    @Override
    public List<User> searchUsers(String username, Integer roleType) {
        return userDao.searchUsers(username, roleType);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status, String comment) {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 更新用户状态
        user.setStatus(status);
        userDao.updateById(user);
        
        // 记录操作日志
        AuditInfo auditInfo = new AuditInfo();
        auditInfo.setObjectId(userId);
        auditInfo.setAuditType(1);  // 用户审核
        auditInfo.setAuditStatus(status);
        auditInfo.setAuditOpinion(comment);
        auditInfoDao.insert(auditInfo);
    }
} 