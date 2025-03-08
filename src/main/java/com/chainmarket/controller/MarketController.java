package com.chainmarket.controller;

import com.chainmarket.dao.GoodsCategoryDao;
import com.chainmarket.entity.Goods;
import com.chainmarket.entity.GoodsCategory;
import com.chainmarket.entity.User;
import com.chainmarket.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/market")
public class MarketController {
    
    @Autowired
    private IGoodsService goodsService;
    
    @Autowired
    private GoodsCategoryDao categoryDao;
    
    @GetMapping("")
    public String market(Model model, 
                         @RequestParam(required = false) Long categoryId,
                         HttpSession session) {
        
        List<GoodsCategory> categories = categoryDao.selectList(null);
        model.addAttribute("categories", categories);
        
        // 获取已上架商品列表
        List<Goods> goodsList;
        if (categoryId != null) {
            goodsList = goodsService.getOnSaleGoodsByCategory(categoryId);
            model.addAttribute("activeCategoryId", categoryId);
        } else {
            goodsList = goodsService.getOnSaleGoods();
        }
        model.addAttribute("goodsList", goodsList);
        
        // 如果用户已登录，获取用户的商品列表
        User user = (User) session.getAttribute("user");
        if (user != null) {
            List<Goods> myGoodsList = goodsService.getUserGoods(user.getUserId());
            model.addAttribute("myGoodsList", myGoodsList);
        }
        
        return "market/index";
    }
} 