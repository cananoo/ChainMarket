package com.chainmarket.controller;

import com.chainmarket.dto.AuditDTO;
import com.chainmarket.service.IAuditService;
import com.chainmarket.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private IAuditService auditService;
    
    @GetMapping("/users")
    @ResponseBody
    public Result<?> getPendingUsers() {
        return Result.success(auditService.getPendingUsers());
    }
    
    @GetMapping("/goods")
    @ResponseBody
    public Result<?> getPendingGoods() {
        return Result.success(auditService.getPendingGoods());
    }
    
    @PostMapping("/user")
    @ResponseBody
    public Result<Void> auditUser(@RequestBody AuditDTO auditDTO) {
        try {
            auditService.auditUser(auditDTO);
            return Result.success("审核成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/goods")
    @ResponseBody
    public Result<Void> auditGoods(@RequestBody AuditDTO auditDTO) {
        try {
            auditService.auditGoods(auditDTO);
            return Result.success("审核成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 