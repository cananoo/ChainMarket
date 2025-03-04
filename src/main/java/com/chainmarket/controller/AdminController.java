package com.chainmarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chainmarket.service.IUserManageService;
import com.chainmarket.service.ISystemParamService;
import com.chainmarket.common.Result;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private IUserManageService userManageService;
    
    @Autowired
    private ISystemParamService systemParamService;
    
    @GetMapping
    public String index() {
        return "admin/index";
    }
    
    @GetMapping("/users")
    @ResponseBody
    public Result<?> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer roleType) {
        return Result.success(userManageService.searchUsers(username, roleType));
    }
    
    @PostMapping("/user/status")
    @ResponseBody
    public Result<Void> updateUserStatus(
            @RequestParam Long userId,
            @RequestParam Integer status,
            @RequestParam(required = false) String comment) {
        userManageService.updateUserStatus(userId, status, comment);
        return Result.success("操作成功");
    }
    
    @GetMapping("/params")
    @ResponseBody
    public Result<?> getParams() {
        return Result.success(systemParamService.getAllParams());
    }
    
    @PostMapping("/params")
    @ResponseBody
    public Result<Void> updateParams(@RequestBody Map<String, String> params) {
        systemParamService.updateParams(params);
        return Result.success("保存成功");
    }
} 