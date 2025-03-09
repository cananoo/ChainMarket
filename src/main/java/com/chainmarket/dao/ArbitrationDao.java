package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.Arbitration;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface ArbitrationDao extends BaseMapper<Arbitration> {
    
    /**
     * 获取待处理的仲裁案件
     */
    @Select("SELECT a.*, o.orderNo, u.username as initiatorName " +
            "FROM arbitration_case a " +
            "LEFT JOIN order_info o ON a.orderId = o.orderId " +
            "LEFT JOIN user_info u ON a.initiatorId = u.userId " +
            "WHERE a.status = 1 " +  // 状态1表示处理中
            "ORDER BY a.createTime DESC")
    List<Arbitration> selectPendingArbitrations();
} 