package com.chainmarket.dto;

import lombok.Data;

@Data
public class OrderReviewDTO {
    private Long orderId;
    private Integer score;  // 评分(1-5分)
    private String content; // 评价内容
}