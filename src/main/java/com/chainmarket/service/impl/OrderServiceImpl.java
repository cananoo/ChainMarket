package com.chainmarket.service.impl;

import com.chainmarket.dao.ChainEvidenceDao;
import com.chainmarket.dao.GoodsDao;
import com.chainmarket.dao.OrderDao;
import com.chainmarket.dao.UserDao;
import com.chainmarket.entity.ChainEvidence;
import com.chainmarket.entity.Goods;
import com.chainmarket.entity.Order;
import com.chainmarket.entity.User;
import com.chainmarket.exception.BusinessException;
import com.chainmarket.service.IOrderService;
import com.chainmarket.service.IWalletService;
import com.chainmarket.util.BcosClientWrapper;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements IOrderService {
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private BcosClientWrapper bcosClientWrapper;
    
    @Autowired
    private IWalletService walletService;
    
    @Autowired
    private ChainEvidenceDao chainEvidenceDao;
    
    private static final String WALLET_ADDRESS = "0x3a20b086b5523c49ea04c2e16ba1dac63f8b51a1";
    private static final String GOODS_ADDRESS = "0xf3de04e031091a612887caaf62928ce49cbb7ebf";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long goodsId, Long buyerId) {
        // 查询商品信息
        Goods goods = goodsDao.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 验证商品状态
        if (goods.getStatus() != 1) {
            throw new BusinessException("商品不可购买");
        }
        
        // 验证是否是自己的商品
        if (goods.getSellerId().equals(buyerId)) {
            throw new BusinessException("不能购买自己的商品");
        }
        
        // 检查是否已有未完成的订单
        if (orderDao.existsUnfinishedOrder(goodsId)) {
            throw new BusinessException("该商品已有未完成的订单");
        }
        
        // 获取买家和卖家信息
        User buyer = userDao.selectById(buyerId);
        User seller = userDao.selectById(goods.getSellerId());
        if (buyer == null || seller == null) {
            throw new BusinessException("用户信息不存在");
        }
        
        // 检查买家余额
        BigDecimal balance = walletService.getBalance(buyer.getWalletAddress());
        if (balance.compareTo(goods.getPrice()) < 0) {
            throw new BusinessException("余额不足，请先充值");
        }
        
        try {
            // 1. 执行钱包合约转账
            List<Object> transferParams = new ArrayList<>();
            transferParams.add(buyer.getWalletAddress());  // from
            transferParams.add(seller.getWalletAddress()); // to
            transferParams.add(goods.getPrice().intValue()); // amount
            
            TransactionResponse transferResponse = bcosClientWrapper.getTransactionProcessor()
                .sendTransactionAndGetResponseByContractLoader(
                    "Wallet",
                    WALLET_ADDRESS,
                    "transfer",
                    transferParams
                );
            
            if (!"Success".equals(transferResponse.getReceiptMessages())) {
                throw new BusinessException("转账失败: " + transferResponse.getReceiptMessages());
            }
            
            // 2. 执行商品所有权转移
            List<Object> transferOwnershipParams = new ArrayList<>();
            transferOwnershipParams.add(goods.getGoodsId());
            transferOwnershipParams.add(buyer.getWalletAddress());
            
            TransactionResponse ownershipResponse = bcosClientWrapper.getTransactionProcessor()
                .sendTransactionAndGetResponseByContractLoader(
                    "Goods",
                    GOODS_ADDRESS,
                    "transferOwnership",
                    transferOwnershipParams
                );
            
            if (!"Success".equals(ownershipResponse.getReceiptMessages())) {
                throw new BusinessException("商品所有权转移失败: " + ownershipResponse.getReceiptMessages());
            }
            
            // 3. 创建订单记录
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setGoodsId(goodsId);
            order.setBuyerId(buyerId);
            order.setSellerId(goods.getSellerId());
            order.setAmount(goods.getPrice());
            order.setTxHash(transferResponse.getTransactionReceipt().getTransactionHash());
            order.setStatus(1);  // 待发货状态
            orderDao.insert(order);
            
            // 4. 更新商品状态
            goods.setStatus(2);  // 已下架状态
            goodsDao.updateById(goods);
            
            // 5. 记录交易存证
            ChainEvidence evidence = new ChainEvidence();
            evidence.setEvidenceType(1);  // 订单交易类型
            evidence.setEvidenceContent(
                String.format("买家(%s)购买了卖家(%s)的商品：(%s)--(%s)，交易金额：%s",
                    buyer.getUsername(), seller.getUsername(), goods.getGoodsId(),
                    goods.getGoodsName(), goods.getPrice())
            );
            evidence.setTxHash(ownershipResponse.getTransactionReceipt().getTransactionHash());
            evidence.setBlockHeight(Long.parseLong(transferResponse.getTransactionReceipt().getBlockNumber().substring(2), 16));
            evidence.setBlockTime(LocalDateTime.now());
            chainEvidenceDao.insert(evidence);
            
            return order;
            
        } catch (Exception e) {
            throw new BusinessException("创建订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        // 生成格式: 年月日时分秒+6位随机数
        String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return timeStr + randomStr;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long buyerId) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证订单所属人
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 验证订单状态
        if (order.getStatus() != 1) { // 应该检查是否为待发货状态
            throw new BusinessException("订单状态错误");
        }
        
        // 生成物流单号
        order.generateTrackingNo();
        
        // 更新订单状态
        order.setStatus(1);  // 保持待发货状态
        order.setPayTime(LocalDateTime.now());
        orderDao.updateById(order);
        
        // 更新商品所有权和状态
        Goods goods = goodsDao.selectById(order.getGoodsId());
        
        // 转移商品所有权
        goods.setSellerId(order.getBuyerId());
        goods.setStatus(2);  // 设置为下架状态
        goodsDao.updateById(goods);
    }
    
    @Override
    public void confirmOrder(Long orderId, Long buyerId) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证订单所属人
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 验证订单状态
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态错误");
        }
        
        // 更新订单状态
        order.setStatus(2);  // 已完成
        order.setReceiveTime(LocalDateTime.now());
        orderDao.updateById(order);
        
        // 更新商品状态为已售出
        Goods goods = goodsDao.selectById(order.getGoodsId());
        goods.setStatus(3);  // 已售出状态
        goodsDao.updateById(goods);
    }
    
    @Override
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证订单所属人(买家或卖家都可以取消)
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 验证订单状态
        if (order.getStatus() != 1) {  // 只有待发货状态可以取消
            throw new BusinessException("当前订单状态不可取消");
        }
        
        // 更新订单状态
        order.setStatus(4);  // 已取消
        orderDao.updateById(order);
        
        // 恢复商品状态为在售
        Goods goods = goodsDao.selectById(order.getGoodsId());
        goods.setStatus(1);  // 在售状态
        goodsDao.updateById(goods);
    }
    

    
    @Override
    public List<Order> getBuyerOrders(Long buyerId) {
        return orderDao.selectByBuyer(buyerId);
    }
    
    @Override
    public List<Order> getSellerOrders(Long sellerId) {
        return orderDao.selectBySeller(sellerId);
    }
    
    @Override
    public Order getOrderDetail(Long orderId) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, Long sellerId) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证卖家身份
        if (!order.getSellerId().equals(sellerId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 验证订单状态
        if (!order.canShip()) {
            throw new BusinessException("当前订单状态不可发货");
        }
        
        // 生成物流单号
        order.generateTrackingNo();
        order.setStatus(2);  // 更新为待收货状态
        order.setShipTime(LocalDateTime.now());
        orderDao.updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrder(Long orderId, Long buyerId) {
        // 查询订单信息
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证买家身份
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 验证订单状态
        if (!order.canReceive()) {
            throw new BusinessException("当前订单状态不可收货");
        }
        
        // 更新订单状态
        order.setStatus(3);  // 已完成
        order.setReceiveTime(LocalDateTime.now());
        orderDao.updateById(order);
        
        // 更新商品所有权
        Goods goods = goodsDao.selectById(order.getGoodsId());
        
        // 转移商品所有权 - 仅更新数据库记录，溯源信息将由区块链处理
        goods.setSellerId(order.getBuyerId());
        goods.setStatus(1);  // 设置为已上架状态，无需再审核
        goodsDao.updateById(goods);
    }
} 