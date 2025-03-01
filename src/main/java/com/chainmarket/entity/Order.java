package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@TableName("order_info")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long orderId;
    
    @TableField("orderNo")
    private String orderNo;
    
    @TableField("goodsId")
    private Long goodsId;
    
    @TableField("buyerId")
    private Long buyerId;
    
    @TableField("sellerId") 
    private Long sellerId;
    
    private BigDecimal amount;
    
    private Integer status;  // 1-待发货 2-待收货 3-已完成 4-已取消
    
    @TableField("trackingNo")
    private String trackingNo;
    
    @TableField("txHash")
    private String txHash;
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField("payTime")
    private LocalDateTime payTime;
    
    @TableField("shipTime")
    private LocalDateTime shipTime;
    
    @TableField("receiveTime")
    private LocalDateTime receiveTime;
    
    /**
     * 生成物流单号
     */
    public void generateTrackingNo() {
        // 格式：CM + 年月日时分秒 + 6位随机数
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        this.trackingNo = "CM" + dateStr + randomStr;
    }
    
    /**
     * 是否可以发货
     */
    public boolean canShip() {
        return status != null && status == 1; // 状态1表示待发货
    }
    
    /**
     * 是否可以收货
     */
    public boolean canReceive() {
        return status != null && status == 2; // 状态2表示待收货
    }
} 