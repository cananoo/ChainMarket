package com.chainmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chainmarket.dao.OrderDao;
import com.chainmarket.dao.OrderReviewDao;
import com.chainmarket.dao.UserDao;
import com.chainmarket.dto.OrderReviewDTO;
import com.chainmarket.entity.Order;
import com.chainmarket.entity.OrderReview;
import com.chainmarket.entity.User;
import com.chainmarket.exception.BusinessException;
import com.chainmarket.service.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewServiceImpl implements IReviewService {

    @Autowired
    private OrderReviewDao orderReviewDao;
    
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private UserDao userDao;

    @Override
    public boolean existsOrderReview(Long orderId) {
        return orderReviewDao.existsByOrderId(orderId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(OrderReviewDTO reviewDTO, Long userId) {
        // 验证订单
        Order order = orderDao.selectById(reviewDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 验证买家身份
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("只有买家才能评价订单");
        }
        
        // 验证订单状态
        if (order.getStatus() != 3) { // 3-已完成
            throw new BusinessException("只能评价已完成的订单");
        }
        
        // 检查是否已评价
        if (existsOrderReview(order.getOrderId())) {
            throw new BusinessException("该订单已评价，不能重复评价");
        }
        
        // 验证评分
        if (reviewDTO.getScore() < 1 || reviewDTO.getScore() > 5) {
            throw new BusinessException("评分必须在1-5分之间");
        }
        
        // 创建评价
        OrderReview review = new OrderReview();
        review.setOrderId(reviewDTO.getOrderId());
        review.setBuyerId(userId);
        review.setScore(reviewDTO.getScore());
        review.setContent(reviewDTO.getContent());
        
        // 保存评价
        orderReviewDao.insert(review);
        
        // 根据评分调整卖家信用分
        User seller = userDao.selectById(order.getSellerId());
        if (seller != null) {
            int creditDelta = 0;
            
            // 根据评分调整信用分
            switch (reviewDTO.getScore()) {
                case 1: creditDelta = -2; break; // 1分减2分
                case 2: creditDelta = -1; break; // 2分减1分
                case 3: creditDelta = 0; break;  // 3分不变
                case 4: creditDelta = 1; break;  // 4分加1分
                case 5: creditDelta = 2; break;  // 5分加2分
                default: creditDelta = 0;
            }
            
            // 更新卖家信用分
            if (creditDelta != 0) {
                Integer currentCredit = seller.getCreditScore();
                if (currentCredit == null) {
                    currentCredit = 80; // 默认信用分
                }
                
                // 直接更新信用分，不限制范围
                int newCredit = currentCredit + creditDelta;
                seller.setCreditScore(newCredit);
                userDao.updateById(seller);
            }
        }
    }
    
    @Override
    public OrderReview getOrderReview(Long orderId) {
        return orderReviewDao.selectOne(new QueryWrapper<OrderReview>().eq("orderId", orderId));
    }
    
    @Override
    public List<OrderReview> getSellerReviews(Long sellerId) {
        // 查询卖家的所有已完成订单
        List<Order> orders = orderDao.selectList(
            new QueryWrapper<Order>()
                .eq("sellerId", sellerId)
                .eq("status", 3)
        );
        
        // 查询这些订单的评价
        if (orders.isEmpty()) {
            return List.of();
        }
        
        return orderReviewDao.selectList(
            new QueryWrapper<OrderReview>()
                .in("orderId", orders.stream().map(Order::getOrderId).toList())
        );
    }
    
    @Override
    public List<OrderReview> getBuyerReviews(Long buyerId) {
        return orderReviewDao.selectList(
            new QueryWrapper<OrderReview>()
                .eq("buyerId", buyerId)
        );
    }
}