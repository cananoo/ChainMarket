package com.chainmarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.chainmarket.service.IUserManageService;
import com.chainmarket.service.ISystemParamService;
import com.chainmarket.service.IGoodsService;
import com.chainmarket.common.Result;
import com.chainmarket.entity.GoodsCategory;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private IUserManageService userManageService;
    
    @Autowired
    private ISystemParamService systemParamService;
    
    @Autowired
    private IGoodsService goodsService;
    
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
    
    /**
     * 获取所有商品类别
     */
    @GetMapping("/categories")
    @ResponseBody
    public Result<?> getAllCategories() {
        return Result.success(goodsService.getCategories());
    }
    
    /**
     * 添加商品类别
     */
    @PostMapping("/category")
    @ResponseBody
    public Result<Void> addCategory(@RequestBody GoodsCategory category) {
        goodsService.addCategory(category);
        return Result.success("添加成功");
    }
    
    /**
     * 更新商品类别
     */
    @PutMapping("/category")
    @ResponseBody
    public Result<Void> updateCategory(@RequestBody GoodsCategory category) {
        goodsService.updateCategory(category);
        return Result.success("更新成功");
    }
    
    /**
     * 删除商品类别
     */
    @DeleteMapping("/category/{categoryId}")
    @ResponseBody
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        goodsService.deleteCategory(categoryId);
        return Result.success("删除成功");
    }
} 