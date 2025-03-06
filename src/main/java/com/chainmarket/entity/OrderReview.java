package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_review")
public class OrderReview {
    
    @TableId(type = IdType.AUTO)
    private Long reviewId;
    
    @TableField("orderId")
    private Long orderId;
    
    @TableField("buyerId")
    private Long buyerId;
    
    private Integer score;  // 评分(1-5分)
    
    private String content;  // 评价内容
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}