package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单评价数据访问接口
 */
@Mapper
public interface OrderReviewDao extends BaseMapper<OrderReview> {
    
    /**
     * 检查订单是否已评价
     */
    default boolean existsByOrderId(Long orderId) {
        return selectCount(new QueryWrapper<OrderReview>().eq("orderId", orderId)) > 0;
    }
}