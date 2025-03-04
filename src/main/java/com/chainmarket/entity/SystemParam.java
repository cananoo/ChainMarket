package com.chainmarket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_param")
public class SystemParam {
    
    @TableId(type = IdType.AUTO)
    private Long paramId;
    
    @TableField("paramKey")
    private String paramKey;
    
    @TableField("paramValue")
    private String paramValue;
    
    @TableField("paramDesc")
    private String paramDesc;
    
    @TableField(value = "createTime", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "updateTime", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 