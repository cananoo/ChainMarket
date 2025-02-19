package com.chainmarket.dto;

import lombok.Data;

@Data
public class AuditDTO {
    private Long id;           // 审核对象ID
    private String type;       // 审核类型：user-用户审核, goods-商品审核
    private Integer status;    // 审核状态：1-通过, 2-拒绝
    private String comment;    // 审核意见
} 