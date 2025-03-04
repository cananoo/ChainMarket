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
    @Select("SELECT * FROM arbitration_case " +
            "WHERE status = 0 " +
            "ORDER BY createTime DESC")
    List<Arbitration> selectPendingArbitrations();
} 