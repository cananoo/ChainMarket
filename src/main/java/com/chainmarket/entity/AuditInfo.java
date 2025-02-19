package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_info")
public class AuditInfo {
    
    @TableId(type = IdType.AUTO)
    private Long auditId;
    
    private Long objectId;
    
    private Integer auditType;  // 1-用户 2-商品
    
    private Integer auditStatus;  // 0-待审核 1-通过 2-拒绝
    
    private String auditOpinion;
    
    private Long auditorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 