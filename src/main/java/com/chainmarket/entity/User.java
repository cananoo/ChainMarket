package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_info")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long userId;
    
    private String username;
    
    private String password;
    
    private String phone;
    
    @TableField("roleType")
    private Integer roleType;  // 1-买家 2-卖家 3-仲裁员 4-管理员
    
    @TableField("status")
    private Integer status;    // 0-待审核 1-正常 2-禁用
    
    @TableField("walletAddress")
    private String walletAddress;  // 用户钱包地址
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 