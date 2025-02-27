package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.GoodsCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类数据访问层
 */
@Mapper
public interface GoodsCategoryDao extends BaseMapper<GoodsCategory> {
    // MyBatis-Plus提供的BaseMapper已经包含了基础的CRUD方法
    // 如果需要自定义查询方法可以在这里添加
} 