package com.chainmarket.controller;

import com.chainmarket.entity.User;
import com.chainmarket.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/market")
public class MarketController {
    
    @Autowired
    private IGoodsService goodsService;
    
    @GetMapping
    public String market(Model model, HttpSession session) {
        // 获取商品分类
        model.addAttribute("categories", goodsService.getCategories());
        // 获取已上架商品列表
        model.addAttribute("goodsList", goodsService.getOnSaleGoods());
        
        // 如果是卖家，获取其商品列表
        User user = (User) session.getAttribute("user");
        if (user != null && user.getRoleType() == 2) {
            model.addAttribute("myGoods", goodsService.getSellerGoods(user.getUserId()));
            model.addAttribute("isSeller", true);
        }
        
        return "market/index";
    }
} 