package com.chainmarket.controller;

import com.chainmarket.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 主页控制器
 */
@Controller
public class IndexController {
    
    @Autowired
    private IGoodsService goodsService;
    
    @GetMapping("/")
    public String index(Model model) {
        // 获取最新上架的商品（限制8个）
        model.addAttribute("latestGoods", goodsService.getLatestGoods(8));
        return "index";
    }
} 