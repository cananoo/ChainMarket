package com.chainmarket.service;

import com.chainmarket.dto.OrderReviewDTO;
import com.chainmarket.entity.OrderReview;
import java.util.List;

/**
 * 评价服务接口
 */
public interface IReviewService {

    /**
     * 检查订单是否已评价
     */
    boolean existsOrderReview(Long orderId);
    
    /**
     * 提交订单评价
     */
    void submitReview(OrderReviewDTO reviewDTO, Long userId);
    
    /**
     * 获取订单评价
     */
    OrderReview getOrderReview(Long orderId);
    
    /**
     * 获取卖家的所有评价
     */
    List<OrderReview> getSellerReviews(Long sellerId);
    
    /**
     * 获取买家的所有评价
     */
    List<OrderReview> getBuyerReviews(Long buyerId);
}
