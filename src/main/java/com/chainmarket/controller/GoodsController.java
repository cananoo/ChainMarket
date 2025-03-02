package com.chainmarket.controller;

import com.chainmarket.dto.GoodsUploadDTO;
import com.chainmarket.entity.Goods;
import com.chainmarket.entity.User;
import com.chainmarket.service.IGoodsService;
import com.chainmarket.service.IImageService;
import com.chainmarket.service.IUserService;
import com.chainmarket.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    
    @Autowired
    private IGoodsService goodsService;
    
    @Autowired
    private IImageService imageService;
    
    @Autowired
    private IUserService userService;
    
    @GetMapping("/upload")
    public String uploadPage() {
        return "goods/upload";
    }
    
    @PostMapping("/upload")
    @ResponseBody
    public Result<Void> upload(@RequestBody GoodsUploadDTO goodsDTO, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        if (user.getRoleType() != 1) {
            return Result.error("只有普通用户才能发布商品");
        }
        
        // 打印关键信息
        System.out.println("商品上传 - 名称: " + goodsDTO.getGoodsName() + ", 图片: " + goodsDTO.getImageUrl());
        
        try {
            goodsService.uploadGoods(goodsDTO, user.getUserId());
            return Result.success("商品上传成功，等待审核");
        } catch (Exception e) {
            System.out.println("商品上传失败: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/categories")
    @ResponseBody
    public Result<?> getCategories() {
        return Result.success(goodsService.getCategories());
    }
    
    @PostMapping("/status")
    @ResponseBody
    public Result<Void> updateStatus(
            @RequestParam Long goodsId,
            @RequestParam Integer status,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            goodsService.updateGoodsStatus(goodsId, status, user.getUserId());
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/upload/image")
    @ResponseBody
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.uploadImage(file);
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                System.out.println("图片上传成功，URL: " + imageUrl);
                return Result.success(imageUrl);
            }
            return Result.error("图片上传失败：未获取到URL");
        } catch (Exception e) {
            System.out.println("图片上传失败: " + e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // 获取商品详情
        Goods goods = goodsService.getGoodsDetail(id);
        model.addAttribute("goods", goods);
        
        // 获取卖家信息
        User seller = userService.getUserById(goods.getSellerId());
        model.addAttribute("seller", seller);
        
        return "goods/detail";
    }
} 