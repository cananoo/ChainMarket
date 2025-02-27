package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@TableName("goods_info")
public class Goods {
    
    @TableId(type = IdType.AUTO)
    private Long goodsId;
    
    @TableField("categoryId")
    private Long categoryId;
    
    @TableField("sellerId")
    private Long sellerId;
    
    @TableField("owner_history")
    private String ownerHistory;
    
    @TableField("goodsName")
    private String goodsName;
    
    @TableField("goodsDesc")
    private String goodsDesc;
    
    private BigDecimal price;
    
    private Integer status;    // 0-待审核 1-已上架 2-已下架 3-已售出
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 添加新的拥有者到历史记录
     * @param ownerId 拥有者ID
     */
    public void addOwner(Long ownerId) {
        if (ownerHistory == null || ownerHistory.isEmpty()) {
            ownerHistory = ownerId.toString();
        } else {
            ownerHistory += "," + ownerId;
        }
    }
    
    /**
     * 获取历史拥有者ID列表
     * @return 拥有者ID列表
     */
    public List<Long> getOwnerList() {
        if (ownerHistory == null || ownerHistory.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(ownerHistory.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
} 