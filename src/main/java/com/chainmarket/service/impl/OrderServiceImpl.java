package com.chainmarket.service.impl;

import com.chainmarket.dao.OrderDao;
import com.chainmarket.dao.GoodsDao;
import com.chainmarket.dao.GoodsHistoryDao;
import com.chainmarket.entity.Order;
import com.chainmarket.entity.Goods;
import com.chainmarket.entity.GoodsHistory;
import com.chainmarket.service.IOrderService;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements IOrderService {
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private GoodsDao goodsDao;
    
    @Autowired
    private GoodsHistoryDao goodsHistoryDao;
    
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
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());  // 生成订单编号
        order.setGoodsId(goodsId);
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getSellerId());
        order.setAmount(goods.getPrice());
        order.setStatus(1);  // 直接设为待发货状态
        
        // 保存订单
        orderDao.insert(order);
        
        // 更新商品状态为已下架
        goods.setStatus(2);  // 设置为已下架状态
        goodsDao.updateById(goods);
        
        return order;
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
        
        // 记录历史所有者
        GoodsHistory history = new GoodsHistory();
        history.setGoodsId(goods.getGoodsId());
        history.setUserId(goods.getSellerId());  // 记录原卖家
        history.setStartTime(goods.getCreateTime());
        history.setEndTime(LocalDateTime.now());
        history.setTxHash(order.getTxHash());
        goodsHistoryDao.insert(history);
        
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
    public void applyArbitration(Long orderId, Long userId, String reason) {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证订单所属人(买家或卖家都可以申请仲裁)
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        
        // 验证订单状态
        if (order.getStatus() != 1) {  // 只有已支付待确认状态可以申请仲裁
            throw new BusinessException("当前订单状态不可申请仲裁");
        }
        
        // 更新订单状态
        order.setStatus(4);  // 申请仲裁状态
        orderDao.updateById(order);
        
        // TODO: 创建仲裁记录
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
        
        // 记录历史所有者
        GoodsHistory history = new GoodsHistory();
        history.setGoodsId(goods.getGoodsId());
        history.setUserId(goods.getSellerId());
        history.setStartTime(goods.getCreateTime());
        history.setEndTime(LocalDateTime.now());
        history.setTxHash(order.getTxHash());
        goodsHistoryDao.insert(history);
        
        // 转移商品所有权
        goods.setSellerId(order.getBuyerId());
        goods.setStatus(1);  // 设置为已上架状态，无需再审核
        goodsDao.updateById(goods);
    }
} 