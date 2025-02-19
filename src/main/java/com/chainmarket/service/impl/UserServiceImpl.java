package com.chainmarket.service.impl;

import com.chainmarket.dao.UserDao;
import com.chainmarket.entity.User;
import com.chainmarket.exception.BusinessException;
import com.chainmarket.service.IUserService;
import com.chainmarket.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;
import com.chainmarket.dto.LoginDTO;
import com.chainmarket.dao.AuditInfoDao;
import com.chainmarket.entity.AuditInfo;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements IUserService {
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired
    private AuditInfoDao auditInfoDao;
    
    private static final String VERIFY_CODE_PREFIX = "verify_code:";
    private static final long VERIFY_CODE_EXPIRE = 5; // 验证码5分钟有效期
    
    @Override
    public String sendVerifyCode(String phone) {
        // 生成6位随机验证码
        String verifyCode = String.format("%06d", (int)(Math.random() * 1000000));
        
        // 将验证码存入Redis并设置过期时间
        String key = VERIFY_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(key, verifyCode, VERIFY_CODE_EXPIRE, TimeUnit.MINUTES);
        
        // 模拟发送验证码
        System.out.println("验证码为：" + verifyCode );


        
        return verifyCode;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(User user, String verifyCode) {
        // 验证用户名是否存在
        if (userDao.checkUsernameExists(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 验证手机号是否存在
        if (userDao.checkPhoneExists(user.getPhone())) {
            throw new BusinessException("手机号已注册");
        }
        
        // 验证验证码
        String key = VERIFY_CODE_PREFIX + user.getPhone();
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode == null || !savedCode.equals(verifyCode)) {
            throw new BusinessException("验证码错误或已过期");
        }
        
        // 密码加密(使用MD5)
        user.setPassword(PasswordEncoder.encode(user.getPassword()));
        // 验证角色类型
        if (user.getRoleType() == null || user.getRoleType() < 1 || user.getRoleType() > 3) {
            throw new BusinessException("无效的角色类型");
        }
        
        user.setStatus(0);    // 默认待审核
        
        // 保存用户信息
        userDao.insert(user);
        
        // 删除验证码
        redisTemplate.delete(key);
    }

    @Override
    public User login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = userDao.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码
        if (!PasswordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号待审核");
        } else if (user.getStatus() == 2) {
            // 查询最新的审核记录
            AuditInfo auditInfo = auditInfoDao.selectLatestByObjectId(user.getUserId(), 1);
            String reason = auditInfo != null && auditInfo.getAuditOpinion() != null ? 
                    auditInfo.getAuditOpinion() : "未提供原因";
            throw new BusinessException("账号已被禁用，原因：" + reason);
        }
        
        return user;
    }
} 