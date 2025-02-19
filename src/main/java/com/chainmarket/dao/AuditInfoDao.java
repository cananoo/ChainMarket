package com.chainmarket.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainmarket.entity.AuditInfo;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Mapper
public interface AuditInfoDao extends BaseMapper<AuditInfo> {
    /**
     * 查询最新的审核记录
     * @param objectId 审核对象ID
     * @param auditType 审核类型
     * @return 审核信息
     */
    default AuditInfo selectLatestByObjectId(Long objectId, Integer auditType) {
        return selectOne(new QueryWrapper<AuditInfo>()
                .eq("objectId", objectId)
                .eq("auditType", auditType)
                .orderByDesc("createTime")
                .last("LIMIT 1"));
    }
} 