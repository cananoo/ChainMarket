package com.chainmarket.service;

import com.chainmarket.entity.Order;
import java.util.List;

public interface IOrderService {
    
    /**
     * 创建订单
     */
    Order createOrder(Long goodsId, Long buyerId);
    
    /**
     * 支付订单
     */
    void payOrder(Long orderId, Long buyerId);
    
    /**
     * 确认收货
     */
    void confirmOrder(Long orderId, Long buyerId);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);
    
    /**
     * 申请仲裁
     */
    void applyArbitration(Long orderId, Long userId, String reason);
    
    /**
     * 获取买家订单列表
     */
    List<Order> getBuyerOrders(Long buyerId);
    
    /**
     * 获取卖家订单列表
     */
    List<Order> getSellerOrders(Long sellerId);
    
    /**
     * 获取订单详情
     */
    Order getOrderDetail(Long orderId);
    
    /**
     * 卖家发货
     */
    void shipOrder(Long orderId, Long sellerId);
    
    /**
     * 买家确认收货
     */
    void receiveOrder(Long orderId, Long buyerId);
} 