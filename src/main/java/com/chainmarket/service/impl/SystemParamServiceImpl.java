package com.chainmarket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chainmarket.dao.SystemParamDao;
import com.chainmarket.entity.SystemParam;
import com.chainmarket.service.ISystemParamService;
import com.chainmarket.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;

@Service
public class SystemParamServiceImpl implements ISystemParamService {
    
    @Autowired
    private SystemParamDao systemParamDao;
    
    private static final String ARBITRATOR_COUNT_KEY = "arbitrator_count";
    
    @Override
    public Map<String, String> getAllParams() {
        Map<String, String> params = new HashMap<>();
        systemParamDao.selectAllParams().forEach(param -> 
            params.put(param.getParamKey(), param.getParamValue())
        );
        return params;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            throw new BusinessException("参数不能为空");
        }
        
        try {
            params.forEach((key, value) -> {
                if (key == null || value == null) {
                    throw new BusinessException("参数键值不能为空");
                }
                
                // 查询现有参数
                SystemParam existingParam = systemParamDao.selectOne(
                    new QueryWrapper<SystemParam>().eq("paramKey", key)
                );
                
                if (existingParam != null) {
                    // 更新参数
                    existingParam.setParamValue(value);
                    int rows = systemParamDao.updateById(existingParam);
                    if (rows != 1) {
                        throw new BusinessException("更新参数失败");
                    }
                } else {
                    // 新增参数
                    SystemParam newParam = new SystemParam();
                    newParam.setParamKey(key);
                    newParam.setParamValue(value);
                    newParam.setParamDesc("仲裁员数量配置");
                    int rows = systemParamDao.insert(newParam);
                    if (rows != 1) {
                        throw new BusinessException("新增参数失败");
                    }
                }
            });
        } catch (Exception e) {
            throw new BusinessException("保存参数失败：" + e.getMessage());
        }
    }
    
    @Override
    public int getArbitratorCount() {
        String value = systemParamDao.getParamValue(ARBITRATOR_COUNT_KEY);
        return value != null ? Integer.parseInt(value) : 3; // 默认3人
    }
} 