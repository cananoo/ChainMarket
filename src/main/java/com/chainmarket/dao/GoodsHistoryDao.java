package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.GoodsHistory;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

@Mapper
public interface GoodsHistoryDao extends BaseMapper<GoodsHistory> {
    
    /**
     * 查询商品的所有历史拥有者
     */
    default List<GoodsHistory> selectByGoodsId(Long goodsId) {
        return selectList(new QueryWrapper<GoodsHistory>()
                .eq("goodsId", goodsId)
                .orderByDesc("endTime"));
    }
    
    /**
     * 查询用户拥有过的所有商品历史
     */
    default List<GoodsHistory> selectByUserId(Long userId) {
        return selectList(new QueryWrapper<GoodsHistory>()
                .eq("userId", userId)
                .orderByDesc("endTime"));
    }
} 