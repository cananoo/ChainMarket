package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.SystemParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SystemParamDao extends BaseMapper<SystemParam> {
    
    @Select("SELECT * FROM sys_param")
    List<SystemParam> selectAllParams();
    
    default String getParamValue(String key) {
        SystemParam param = selectOne(new QueryWrapper<SystemParam>()
                .eq("paramKey", key));
        return param != null ? param.getParamValue() : null;
    }
} 