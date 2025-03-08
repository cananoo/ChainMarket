package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chain_evidence")
public class ChainEvidence {
    
    @TableId(type = IdType.AUTO)
    private Long evidenceId;
    
    private Integer evidenceType;
    
    private String evidenceContent;
    
    private String txHash;
    
    private Long blockHeight;
    
    private LocalDateTime blockTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 