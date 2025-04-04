package com.chainmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chainmarket.dao.*;
import com.chainmarket.entity.*;
import com.chainmarket.service.IArbitrationService;
import com.chainmarket.service.IImageService;
import com.chainmarket.service.ISystemParamService;
import com.chainmarket.util.BcosClientWrapper;
import com.chainmarket.util.SnowflakeIdGenerator;
import com.chainmarket.exception.BusinessException;

import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.TransactionBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.util.List;
import java.util.Iterator;

import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class ArbitrationServiceImpl implements IArbitrationService {
    
    private static final String WALLET_ADDRESS = "0x3a20b086b5523c49ea04c2e16ba1dac63f8b51a1";
    private static final String ARBITRATION_CONTRACT_ADDRESS = "0x4a6f96c4f8100cce3253616f7722c0f7e7beb542";
    private static final String GOODS_CONTRACT_ADDRESS = "0xf3de04e031091a612887caaf62928ce49cbb7ebf";

    private static final String ARBITRATION_EXPIRE_DAYS_KEY = "arbitration_expire_days";

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

    @Autowired
    private BcosClientWrapper bcosClientWrapper;

    @Autowired
    private ChainEvidenceDao chainEvidenceDao;


    
    @Override
    //获取所有待仲裁案件
    public List<Arbitration> getPendingArbitrations() throws ABICodecException, TransactionBaseException {
        // 获取待处理的仲裁案件
        List<Arbitration> arbitrations = arbitrationDao.selectPendingArbitrations();

        // 检查案件是否过期并移除过期案件并存证案件
        int size = arbitrations.size();
        for (int i = 0;i < size;i++) {
            Arbitration arbitration = arbitrations.get(0);
            String caseId = arbitration.getCaseId().toString();
            String caseNo = arbitration.getCaseNo();
            String key = caseId + "total_votes";
            String totalVotesStr = redisTemplate.opsForValue().get(key);
            if (totalVotesStr == null) {

                List<Object> params = new ArrayList<>();
                params.add(caseId);
                params.add(caseNo.toString());
                params.add("无结果");
                params.add(false);
                params.add("案件过期自动失效");
               
                // 调用合约方法存证
                AssembleTransactionProcessor processor = bcosClientWrapper.getTransactionProcessor();
              TransactionReceipt transactionReceipt = processor.sendTransactionAndGetResponseByContractLoader(
                                             "ArbitrationEvidence",
                                              ARBITRATION_CONTRACT_ADDRESS,
                                              "recordResult",
                                              params
                                            ).getTransactionReceipt();
                String txHash = transactionReceipt.getTransactionHash();
                String blockNumber = transactionReceipt.getBlockNumber();
                // 将存证内容写入存证表
                ChainEvidence evidence = new ChainEvidence();
                evidence.setEvidenceType(2);  // 仲裁类型
                evidence.setEvidenceContent(
                "案件过期自动失效"
                );
                evidence.setTxHash(txHash);
                evidence.setBlockHeight(Long.parseLong(blockNumber.substring(2), 16));
                evidence.setBlockTime(LocalDateTime.now());
                chainEvidenceDao.insert(evidence);
           
                        // 设置案件hash和完成时间
                        arbitration.setTxHash(txHash);
                        arbitration.setCompleteTime(LocalDateTime.now());
                        arbitrationDao.updateById(arbitration);
            }
            arbitrations.remove(0);
        }

        
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
        
        // 9.redis设置案件时限为arbitration_expire_days天数
        String expireDaysStr = systemParamService.getParamValue(ARBITRATION_EXPIRE_DAYS_KEY);
        int expireDays = (expireDaysStr != null) ? Integer.parseInt(expireDaysStr) : 3; // 默认3天

        redisTemplate.opsForValue().set(arbitration.getCaseId().toString(), arbitration.getCaseId().toString(), expireDays, TimeUnit.DAYS);
        
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
            redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + arbitrator.getUserId().toString(), "null", expireDays, TimeUnit.DAYS);
        }
      
        // 12.redis初始化投票 key为案件号+yes/no，value为投票人数，初始为0
        redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + "yes", "0", expireDays, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + "no", "0", expireDays, TimeUnit.DAYS);
        // 初始化总投票人数
        redisTemplate.opsForValue().set(arbitration.getCaseId().toString() + "total_votes", "0", expireDays, TimeUnit.DAYS);
      
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
    public void vote(Long caseId, Long userId, boolean approve) throws ABICodecException, TransactionBaseException {
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

           // 11.案件存证
              // 调用合约方法存证
              List<Object> params = new ArrayList<>();
              params.add(caseId);
              params.add(arbitration.getCaseNo().toString());
              params.add(yesCount+":"+ noCount);
              params.add(true);
              params.add("达成仲裁，买家胜诉");
             

              AssembleTransactionProcessor processor = bcosClientWrapper.getTransactionProcessor();
              TransactionReceipt transactionReceipt = processor.sendTransactionAndGetResponseByContractLoader(
                                             "ArbitrationEvidence",
                                              ARBITRATION_CONTRACT_ADDRESS,
                                              "recordResult",
                                              params
                                            ).getTransactionReceipt();
                String txHash = transactionReceipt.getTransactionHash();
                String blockNumber = transactionReceipt.getBlockNumber();
                // 将存证内容写入存证表
                ChainEvidence evidence = new ChainEvidence();
                evidence.setEvidenceType(2);  // 仲裁类型
                evidence.setEvidenceContent("达成仲裁，买家胜诉");
                evidence.setTxHash(txHash);
                evidence.setBlockHeight(Long.parseLong(blockNumber.substring(2), 16));
                evidence.setBlockTime(LocalDateTime.now());
                chainEvidenceDao.insert(evidence);

                // 12. 交易回溯
                 // 返还余额
                 List<Object> walletParams = new ArrayList<>();
                 walletParams.add(userDao.selectById(order.getSellerId()).getWalletAddress());//卖家钱包地址
                 walletParams.add(userDao.selectById(order.getBuyerId()).getWalletAddress());//买家钱包地址
                 walletParams.add(order.getAmount().intValue());//金额
              
                  String arbitrageTxHash = processor.sendTransactionAndGetResponseByContractLoader(
                                      "Wallet",
                                      WALLET_ADDRESS,
                                      "transfer",
                                      walletParams
                                    ).getTransactionReceipt().getTransactionHash();;

                  // 设置案件hash和完成时间
                 arbitration.setTxHash(arbitrageTxHash);
                 arbitration.setCompleteTime(LocalDateTime.now());
                 arbitrationDao.updateById(arbitration);

                  // 13.商品回溯
                  List<Object> goodsParams = new ArrayList<>();
                  goodsParams.add(order.getGoodsId());//商品id
                  goodsParams.add(userDao.selectById(order.getSellerId()).getWalletAddress());//新物主钱包地址
                  
                  TransactionReceipt goodsReceipt = processor.sendTransactionAndGetResponseByContractLoader(
                                      "Goods",
                                      GOODS_CONTRACT_ADDRESS,
                                      "transferOwnership",
                                      goodsParams
                                    ).getTransactionReceipt();

                  String goodsTxHash = goodsReceipt.getTransactionHash();
                  String goodsBlockNumber = goodsReceipt.getBlockNumber();
                  // 将存证内容写入存证表
                  ChainEvidence newOwnerEvidence = new ChainEvidence();
                  newOwnerEvidence.setEvidenceType(1);  // 仲裁类型
                  newOwnerEvidence.setEvidenceContent(
                    String.format("买家(%s)转回了卖家(%s)的商品：(%s)--(%s)，收到退款：%s",
                   userDao.selectById(order.getBuyerId()).getUsername(),
                   userDao.selectById(order.getSellerId()).getUsername(),
                   order.getGoodsId(),
                   goodsDao.selectById(order.getGoodsId()).getGoodsName(),
                   order.getAmount())
                  );
                  newOwnerEvidence.setTxHash(goodsTxHash);
                  newOwnerEvidence.setBlockHeight(Long.parseLong(goodsBlockNumber.substring(2), 16));
                  newOwnerEvidence.setBlockTime(LocalDateTime.now());
                  chainEvidenceDao.insert(newOwnerEvidence);


                  // 在数据库中更改商品卖家
                  Goods goods = goodsDao.selectById(order.getGoodsId());
                  goods.setSellerId(order.getSellerId());
                  goodsDao.updateById(goods);


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
            order.setStatus(3);//不取消
            
            // 10. 根据投票结果处理订单
            // 获取信用分前n位投票者，n为系统参数
            Integer n = systemParamService.getArbitratorCount();


            // 11.案件存证
              // 调用合约方法存证
              List<Object> params = new ArrayList<>();
              params.add(caseId);
              params.add(arbitration.getCaseNo().toString());
              params.add(yesCount+":"+ noCount);
              params.add(false);
              params.add("达成仲裁，卖家胜诉");

              AssembleTransactionProcessor processor = bcosClientWrapper.getTransactionProcessor();
              TransactionReceipt transactionReceipt = processor.sendTransactionAndGetResponseByContractLoader(
                                             "ArbitrationEvidence",
                                              ARBITRATION_CONTRACT_ADDRESS,
                                              "recordResult",
                                              params
                                            ).getTransactionReceipt();
                String txHash = transactionReceipt.getTransactionHash();
                String blockNumber = transactionReceipt.getBlockNumber();
                // 将存证内容写入存证表
                ChainEvidence evidence = new ChainEvidence();
                evidence.setEvidenceType(2);  // 仲裁类型
                evidence.setEvidenceContent("达成仲裁，卖家胜诉");
                evidence.setTxHash(txHash);
                evidence.setBlockHeight(Long.parseLong(blockNumber.substring(2), 16));
                evidence.setBlockTime(LocalDateTime.now());
                chainEvidenceDao.insert(evidence);

                        // 设置案件hash和完成时间
                        arbitration.setTxHash(txHash);
                        arbitration.setCompleteTime(LocalDateTime.now());
                        arbitrationDao.updateById(arbitration);



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

    @Override
    public List<Arbitration> getCompletedArbitrations() throws ABICodecException, TransactionBaseException {
        List<Arbitration> completedCases = arbitrationDao.selectCompletedArbitrations();
        
        // 获取交易处理器
        AssembleTransactionProcessor processor = bcosClientWrapper.getTransactionProcessor();
        
        // 设置每个案件的仲裁结果
        for (Arbitration arbitration : completedCases) {
            try {
                // 调用合约获取仲裁结果
                List<Object> params = new ArrayList<>();
                params.add(arbitration.getCaseId());
                
                TransactionResponse response = processor.sendTransactionAndGetResponseByContractLoader(
                    "ArbitrationEvidence",
                    ARBITRATION_CONTRACT_ADDRESS,
                    "getResult",
                    params
                );

                String values = response.getValues();
                // 移除开头的 [ 和结尾的 ]
                values = values.substring(1, values.length() - 1);
                // 按照逗号分割，但忽略引号内的逗号
                String[] valuesArray = values.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                // 直接设置投票数
                String voteRatio = valuesArray[1].replace("\"", ""); // 移除引号
                arbitration.setVoteRatio(voteRatio); // 直接使用合约返回的投票数，格式如 "2:1"
                
                // 设置结果
                arbitration.setResult(Boolean.parseBoolean(valuesArray[2]));
                
                // 设置结果描述
                String desc = valuesArray[3].replace("\"", ""); // 移除引号
                arbitration.setResultDesc(desc);
                
                arbitrationDao.updateById(arbitration);
                
            } catch (Exception e) {
                // 记录错误但继续处理其他案件
                System.err.println("处理案件 " + arbitration.getCaseId() + " 时出错: " + e.getMessage());
                // 设置默认值
                arbitration.setVoteRatio("0:0");
                arbitration.setResult(false);
                arbitration.setResultDesc("获取结果失败");
            }
        }
        
        return completedCases;
    }
} 