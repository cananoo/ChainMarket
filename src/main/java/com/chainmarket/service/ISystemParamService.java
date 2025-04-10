package com.chainmarket.service;

import java.util.Map;

public interface ISystemParamService {
    
    /**
     * 获取所有系统参数
     */
    Map<String, String> getAllParams();
    
    /**
     * 更新系统参数
     */
    void updateParams(Map<String, String> params);
    
    /**
     * 获取仲裁员数量
     */
    int getArbitratorCount();
    
    /**
     * 根据参数键获取参数值
     */
    String getParamValue(String key);
    
    /**
     * 获取仲裁参与时间间隔(天)
     */
    int getArbitrationIntervalDays();
} 