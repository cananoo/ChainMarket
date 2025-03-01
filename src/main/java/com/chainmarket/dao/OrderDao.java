package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

@Mapper
public interface OrderDao extends BaseMapper<Order> {
    
    /**
     * 查询用户的订单列表(买家)
     */
    default List<Order> selectByBuyer(Long buyerId) {
        return selectList(new QueryWrapper<Order>()
                .eq("buyerId", buyerId)
                .orderByDesc("createTime"));
    }
    
    /**
     * 查询用户的订单列表(卖家)
     */
    default List<Order> selectBySeller(Long sellerId) {
        return selectList(new QueryWrapper<Order>()
                .eq("sellerId", sellerId)
                .orderByDesc("createTime"));
    }
    
    /**
     * 检查商品是否已有未完成的订单
     */
    default boolean existsUnfinishedOrder(Long goodsId) {
        return exists(new QueryWrapper<Order>()
                .eq("goodsId", goodsId)
                .in("status", 0, 1)); // 待支付或待确认状态
    }
} 