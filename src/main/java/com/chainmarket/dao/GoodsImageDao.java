package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.GoodsImage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsImageDao extends BaseMapper<GoodsImage> {
    // MyBatis-Plus提供的BaseMapper已经包含了基础的CRUD方法
} 