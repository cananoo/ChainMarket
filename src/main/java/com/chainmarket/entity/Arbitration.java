package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("arbitration_case")
public class Arbitration {
    
    @TableId(type = IdType.AUTO)
    private Long caseId;
    
    @TableField("caseNo")
    private String caseNo;
    
    @TableField("orderId")
    private Long orderId;
    
    @TableField("initiatorId")
    private Long initiatorId;
    
    @TableField("disputeDesc")
    private String disputeDesc;
    
    private Integer status;    // 0-待处理 1-处理中 2-已完成
    
    @TableField("txHash")
    private String txHash;
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("completeTime")
    private LocalDateTime completeTime;
    
    @TableField(exist = false)
    private Integer arbitratorCount;
} 