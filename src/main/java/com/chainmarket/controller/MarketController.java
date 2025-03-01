package com.chainmarket.controller;

import com.chainmarket.entity.User;
import com.chainmarket.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/market")
public class MarketController {
    
    @Autowired
    private IGoodsService goodsService;
    
    @GetMapping
    public String market(Model model, HttpSession session, @RequestParam(required = false) Long categoryId) {
        // 获取商品分类
        model.addAttribute("categories", goodsService.getCategories());
        
        // 获取当前选中的分类
        model.addAttribute("currentCategoryId", categoryId);
        
        // 获取已上架商品列表
        if (categoryId != null) {
            model.addAttribute("goodsList", goodsService.getOnSaleGoodsByCategory(categoryId));
        } else {
            model.addAttribute("goodsList", goodsService.getOnSaleGoods());
        }
        
        return "market/index";
    }
} 