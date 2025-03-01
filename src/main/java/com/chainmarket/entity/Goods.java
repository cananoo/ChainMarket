package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods_info")
public class Goods {
    
    @TableId(type = IdType.AUTO)
    private Long goodsId;
    
    @TableField("categoryId")
    private Long categoryId;
    
    @TableField("sellerId")
    private Long sellerId;
    
    @TableField("goodsName")
    private String goodsName;
    
    @TableField("goodsDesc")
    private String goodsDesc;
    
    private BigDecimal price;
    
    private Integer status;    // 0-待审核 1-已上架 2-已下架
    
    @TableField("imageUrl")
    private String imageUrl;   // 商品图片URL
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 