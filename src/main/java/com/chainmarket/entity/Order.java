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
    
    // 商品名称（非数据库字段）
    @TableField(exist = false)
    private String goodsName;
    
    @TableField("buyerId")
    private Long buyerId;
    
    @TableField("sellerId") 
    private Long sellerId;
    
    private BigDecimal amount;
    
    private Integer status;  // 0-待支付 1-待确认 2-已完成 3-已取消 4-退款中 5-已退款
    
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
    
    // 订单状态文本（非数据库字段）
    @TableField(exist = false)
    private String statusText;
    
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
    
    // 获取状态文本
    public String getStatusText() {
        if (status == null) return "未知状态";
        switch (status) {
            case 0: return "待支付";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            case 4: return "仲裁中";
            case 5: return "已退款";
            default: return "未知状态";
        }
    }
    
    // 设置状态文本
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
} 