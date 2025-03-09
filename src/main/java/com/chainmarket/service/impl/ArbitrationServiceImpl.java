package com.chainmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chainmarket.dao.ArbitrationDao;
import com.chainmarket.dao.ArbitrationEvidenceDao;
import com.chainmarket.dao.OrderDao;
import com.chainmarket.dao.UserDao;
import com.chainmarket.dao.GoodsDao;
import com.chainmarket.entity.*;
import com.chainmarket.service.IArbitrationService;
import com.chainmarket.service.IImageService;
import com.chainmarket.service.ISystemParamService;
import com.chainmarket.util.SnowflakeIdGenerator;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

import java.util.List;
import java.util.Iterator;

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
    private UserDao userDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private IImageService imageService;
    
    @Autowired
    private SnowflakeIdGenerator idGenerator;
    
    @Autowired
    private ISystemParamService systemParamService;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Override
    //获取所有待仲裁案件
    public List<Arbitration> getPendingArbitrations() {
        // 获取待处理的仲裁案件
        List<Arbitration> arbitrations = arbitrationDao.selectPendingArbitrations();
        
        // 获取仲裁员数量
        Integer arbitratorCount = systemParamService.getArbitratorCount();
        
        // 为每个案件设置仲裁员数量和已投票人数
        for (Arbitration arbitration : arbitrations) {
            // 设置仲裁员总数
            arbitration.setArbitratorCount(arbitratorCount);
            
            // 获取已投票人数
            String totalVotesKey = arbitration.getCaseId().toString() + "total_votes";
            String totalVotesStr = redisTemplate.opsForValue().get(totalVotesKey);
            int votedCount = totalVotesStr != null ? Integer.parseInt(totalVotesStr) : 0;
            
            // 设置已投票人数
            arbitration.setVotedCount(votedCount);
        }
        
        return arbitrations;
    }
    
    @Override
    @Transactional
    public void applyArbitration(Long orderId, Long userId, String disputeDesc, 
                                MultipartFile evidenceFile, String evidenceDesc) {
        // 1. 验证订单是否存在
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 2. 验证用户是否为买家
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("只有买家才能申请仲裁");
        }
        

        
        // 4. 检查是否已经存在该订单的仲裁案件
        Long count = arbitrationDao.selectCount(
            new QueryWrapper<Arbitration>()
                .eq("orderId", orderId)
        );
        if (count > 0) {
            throw new BusinessException("该订单已存在仲裁案件");
        }
        
        // 5. 创建仲裁案件
        Arbitration arbitration = new Arbitration();
        arbitration.setCaseNo(String.valueOf(idGenerator.nextId())); // 生成案件编号
        arbitration.setOrderId(orderId);
        arbitration.setInitiatorId(userId);
        arbitration.setDisputeDesc(disputeDesc);
        arbitration.setStatus(1); // 处理中
        
        arbitrationDao.insert(arbitration);

        // 将仲裁订单相关的商品下架
        Goods goods = goodsDao.selectById(order.getGoodsId());
        goods.setStatus(2); // 假设2表示已下架状态
        goodsDao.updateById(goods);
         


        
        // 6. 上传证据文件
        String evidenceUrl = imageService.uploadImage(evidenceFile);
        
        // 7. 保存证据信息
        ArbitrationEvidence evidence = new ArbitrationEvidence();
        evidence.setCaseId(arbitration.getCaseId());
        evidence.setUserId(userId);
        evidence.setEvidenceUrl(evidenceUrl);
        evidence.setDescription(evidenceDesc);
        
        arbitrationEvidenceDao.insert(evidence);
        
        // 8. 更新订单状态为仲裁中
        order.setStatus(4); // 状态4表示仲裁中
        orderDao.updateById(order);
        
        // 9.redis设置案件时限为3天
        redisTemplate.opsForValue().set(arbitration.getCaseId().toString(), arbitration.getCaseId().toString(), 3, TimeUnit.DAYS);
        
        // 10.按信用分选取n位仲裁员，n为系统参数
        Integer n = systemParamService.getArbitratorCount();

        // 获取订单买卖双方ID
        order = orderDao.selectById(arbitration.getOrderId());
        Long buyerId = order.getBuyerId();
        Long sellerId = order.getSellerId();

        // 排除买卖双方的仲裁员选择
        List<User> arbitrators = userDao.selectList(
            new QueryWrapper<User>()
                .ne("userId", buyerId)  // 排除买家
                .ne("userId", sellerId) // 排除卖家
                .ne("userId", userId)   // 排除申请人（可能是买家或卖家）
                .orderByDesc("creditScore")
                .last("limit " + n)
        );

        // 如果没有足够的仲裁员
        if (arbitrators.size() < n) {
            throw new BusinessException("系统中没有足够的仲裁员，请联系管理员");
        }

        // 11.将每一位仲裁员信息写入redis，key为案件号+仲裁员id，value为仲裁员仲裁选择（yes/no），初始为"null"
        for (User arbitrator : arbitrators) {
            redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + arbitrator.getUserId().toString(), "null", 3, TimeUnit.DAYS);
        }
      
        // 12.redis初始化投票 key为案件号+yes/no，value为投票人数，初始为0
        redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + "yes", "0", 3, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + "no", "0", 3, TimeUnit.DAYS);
        // 初始化总投票人数
        redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + "total_votes", "0", 3, TimeUnit.DAYS);
      
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
    public List<ArbitrationEvidence> getArbitrationEvidences(Long caseId) {
        return arbitrationEvidenceDao.selectList(
            new QueryWrapper<ArbitrationEvidence>()
                .eq("caseId", caseId)
                .orderByAsc("createTime")
        );
    }

    /**
     * 检查用户是否是案件的仲裁员
     */
    private boolean isArbitrator(Long caseId, Long userId) {
        // 从Redis中检查是否存在该仲裁员的记录
        String key = caseId.toString() + userId.toString();
        return redisTemplate.hasKey(key);
    }

    /**
     * 仲裁员投票
     */
    @Override
    @Transactional
    public void vote(Long caseId, Long userId, boolean approve) {
        // 1. 验证仲裁案件是否存在
        Arbitration arbitration = arbitrationDao.selectById(caseId);
        if (arbitration == null) {
            throw new BusinessException("仲裁案件不存在");
        }
        
        // 2. 验证仲裁案件状态
        if (arbitration.getStatus() != 1) {
            throw new BusinessException("仲裁案件已结束，无法投票");
        }
        
        // 3. 验证用户是否为仲裁员
        if (!isArbitrator(caseId, userId)) {
            throw new BusinessException("您不是该案件的仲裁员");
        }
        
        // 4. 验证是否已投票
        String voteValue = (String) redisTemplate.opsForValue().get(caseId.toString() + userId.toString());
        if (!"null".equals(voteValue)) {
            throw new BusinessException("您已经投过票了");
        }
        
        // 5. 记录投票
        String vote = approve ? "yes" : "no";
        redisTemplate.opsForValue().set(caseId.toString() + userId.toString(), vote, 3, TimeUnit.DAYS);
        
        // 6. 更新投票计数
        String countKey = caseId.toString() + vote;
        String currentCountStr = redisTemplate.opsForValue().get(countKey);
        int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        redisTemplate.opsForValue().set(countKey, String.valueOf(currentCount + 1), 3, TimeUnit.DAYS);
        
        // 6.1 更新总投票人数
        String totalVotesKey = caseId.toString() + "total_votes";
        String totalVotesStr = redisTemplate.opsForValue().get(totalVotesKey);
        int totalVotes = totalVotesStr != null ? Integer.parseInt(totalVotesStr) : 0;
        redisTemplate.opsForValue().set(totalVotesKey, String.valueOf(totalVotes + 1), 3, TimeUnit.DAYS);
        
        // 7. 检查赞成票是否达到所需票数
        String yesCountStr = redisTemplate.opsForValue().get(caseId.toString() + "yes");
        int yesCount = yesCountStr != null ? Integer.parseInt(yesCountStr) : 0;
        //  检查反对票是否达到所需票数
        String noCountStr = redisTemplate.opsForValue().get(caseId.toString() + "no");
        int noCount = noCountStr != null ? Integer.parseInt(noCountStr) : 0;


         Integer arbitratorCount = systemParamService.getArbitratorCount();
        Integer requiredVotes = (arbitratorCount / 2) + 1; // N/2 + 1


        
        if (yesCount >= requiredVotes) {
            // 8. 达成仲裁，更新案件状态
            arbitration.setStatus(2); // 已完成
            arbitrationDao.updateById(arbitration);
            
            // 9. 处理订单状态
            Long orderId = arbitration.getOrderId();
            Order order = orderDao.selectById(orderId);
            order.setStatus(4);//已取消
            
            // 10. 根据投票结果处理订单
            // 获取信用分前n位投票者，n为系统参数
            Integer n = systemParamService.getArbitratorCount();

            // 获取订单买卖双方ID
            Long buyerId = order.getBuyerId();
            Long sellerId = order.getSellerId();

            // 排除买卖双方的仲裁员选择
            List<User> voters = userDao.selectList(
                new QueryWrapper<User>()
                    .ne("userId", buyerId)  // 排除买家
                    .ne("userId", sellerId) // 排除卖家
                    .orderByDesc("creditScore")
                    .last("limit " + n)
            );

                // 增加所有赞成票的投票者信用分
                for (User voter : voters) {
                    // 是否投了赞成票
                    String voteYes = (String) redisTemplate.opsForValue().get(caseId.toString() + voter.getUserId().toString());
                    if ("yes".equals(voteYes)) {
                        voter.setCreditScore(voter.getCreditScore() + 2);
                        userDao.updateById(voter);
                    }
                }
                 // 减少所有反对票的投票者信用分
                for (User voter : voters) {
                    // 是否投了反对票
                    String voteNo = (String) redisTemplate.opsForValue().get(caseId.toString() + voter.getUserId().toString());
                    if ("no".equals(voteNo) || "null".equals(voteNo)) {
                        voter.setCreditScore(voter.getCreditScore() - 2);
                        userDao.updateById(voter);
                    }
                }
                
            
            
            orderDao.updateById(order);
        }else if (noCount >= requiredVotes) {
            // 8. 达成仲裁，更新案件状态
            arbitration.setStatus(2); // 已完成
            arbitrationDao.updateById(arbitration);

             // 9. 处理订单状态
            Long orderId = arbitration.getOrderId();
            Order order = orderDao.selectById(orderId);
            order.setStatus(4);//已取消
            
            // 10. 根据投票结果处理订单
            // 获取信用分前n位投票者，n为系统参数
            Integer n = systemParamService.getArbitratorCount();

            // 获取订单买卖双方ID
            Long buyerId = order.getBuyerId();
            Long sellerId = order.getSellerId();

            // 排除买卖双方的仲裁员选择
            List<User> voters = userDao.selectList(
                new QueryWrapper<User>()
                    .ne("userId", buyerId)  // 排除买家
                    .ne("userId", sellerId) // 排除卖家
                    .orderByDesc("creditScore")
                    .last("limit " + n)
            );

            // 增加所有反对票的投票者信用分
            for (User voter : voters) {
                // 是否投了反对票
                String voteNo = (String) redisTemplate.opsForValue().get(caseId.toString() + voter.getUserId().toString());
                if ("no".equals(voteNo) || "null".equals(voteNo)) {
                    voter.setCreditScore(voter.getCreditScore() + 2);
                    userDao.updateById(voter);
                }
            }

            // 减少所有赞成票的投票者信用分
            for (User voter : voters) {
                // 是否投了赞成票
                String voteYes = (String) redisTemplate.opsForValue().get(caseId.toString() + voter.getUserId().toString());
                if ("yes".equals(voteYes) || "null".equals(voteYes)) {
                    voter.setCreditScore(voter.getCreditScore() - 2);
                    userDao.updateById(voter);
                }
            }

            orderDao.updateById(order);
        }


    }

    @Override
    @Transactional
    public void joinArbitration(Long caseId, Long userId) {
        // 由于我们现在使用自动选择仲裁员的方式，这个方法可以简单实现
        // 检查用户是否是该案件的仲裁员
        if (isArbitrator(caseId, userId)) {
            // 用户已经是仲裁员，不需要再加入
            return;
        } else {
            throw new BusinessException("您不是该案件的仲裁员，无法加入");
        }
    }
} 