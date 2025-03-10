package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.SystemParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SystemParamDao extends BaseMapper<SystemParam> {
    
    /**
     * 获取所有系统参数
     */
    @Select("SELECT * FROM sys_param")
    List<SystemParam> selectAllParams();
    
    /**
     * 根据参数键获取参数值
     * 使用MyBatis注解方式实现
     */
    @Select("SELECT paramValue FROM sys_param WHERE paramKey = #{key}")
    String getParamValueByKey(String key);
    
    /**
     * 根据参数键获取参数值
     * 使用MyBatis-Plus的QueryWrapper实现
     */
    default String getParamValue(String key) {
        SystemParam param = selectOne(new QueryWrapper<SystemParam>()
                .eq("paramKey", key));
        return param != null ? param.getParamValue() : null;
    }
} 