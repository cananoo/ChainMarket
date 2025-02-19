package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GoodsDao extends BaseMapper<Goods> {
    
    /**
     * 查询待审核商品
     */
    default List<Goods> selectPendingGoods() {
        return selectList(new QueryWrapper<Goods>().eq("status", 0));
    }
} 