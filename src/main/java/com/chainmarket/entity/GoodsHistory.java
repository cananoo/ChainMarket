package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("goods_history")
public class GoodsHistory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("goodsId")
    private Long goodsId;
    
    @TableField("userId")
    private Long userId;  // 历史拥有者ID
    
    @TableField("startTime")
    private LocalDateTime startTime;  // 开始拥有时间
    
    @TableField("endTime")
    private LocalDateTime endTime;    // 结束拥有时间
    
    @TableField("txHash")
    private String txHash;  // 交易哈希
} 