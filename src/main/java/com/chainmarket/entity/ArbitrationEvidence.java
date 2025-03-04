package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("arbitration_evidence")
public class ArbitrationEvidence {
    
    @TableId(type = IdType.AUTO)
    private Long evidenceId;
    
    @TableField("caseId")
    private Long caseId;
    
    @TableField("userId")
    private Long userId;
    
    @TableField("evidenceUrl")
    private String evidenceUrl;
    
    private String description;
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 