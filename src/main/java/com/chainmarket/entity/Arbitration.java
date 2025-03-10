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
    
    @TableField(exist = false)
    private Integer votedCount;
    
    @TableField(exist = false)
    private String voteRatio; // 投票比例
    
    @TableField(exist = false)
    private Boolean result; // true表示支持申请人，false表示驳回申请
    
    @TableField(exist = false)
    private String resultDesc; // 仲裁结果描述
    
    public String getResultDesc() {
        if (status != 2) { // 未完成
            return "仲裁进行中";
        }
        // 如果有自定义描述，使用自定义描述
        if (resultDesc != null && !resultDesc.isEmpty()) {
            return resultDesc;
        }
        // 否则使用默认描述
        return result ? "仲裁成功" : "驳回申请";
    }
} 