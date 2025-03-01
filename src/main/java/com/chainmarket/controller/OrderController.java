package com.chainmarket.controller;

import com.chainmarket.entity.Order;
import com.chainmarket.entity.User;
import com.chainmarket.service.IOrderService;
import com.chainmarket.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/order")
public class OrderController {
    
    @Autowired
    private IOrderService orderService;
    
    @PostMapping("/create/{goodsId}")
    @ResponseBody
    public Result<Void> createOrder(@PathVariable Long goodsId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            Order order = orderService.createOrder(goodsId, user.getUserId());
            orderService.payOrder(order.getOrderId(), user.getUserId());
            return Result.success("购买成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/pay/{orderId}")
    @ResponseBody
    public Result<Void> payOrder(@PathVariable Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            orderService.payOrder(orderId, user.getUserId());
            return Result.success("支付成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/ship/{orderId}")
    @ResponseBody
    public Result<Void> shipOrder(@PathVariable Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            orderService.shipOrder(orderId, user.getUserId());
            return Result.success("发货成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/receive/{orderId}")
    @ResponseBody
    public Result<Void> receiveOrder(@PathVariable Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            orderService.receiveOrder(orderId, user.getUserId());
            return Result.success("收货成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public String orderList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }
        
        List<Order> buyOrders = orderService.getBuyerOrders(user.getUserId());
        List<Order> sellOrders = orderService.getSellerOrders(user.getUserId());
        
        if (buyOrders == null) {
            buyOrders = new ArrayList<>();
        }
        if (sellOrders == null) {
            sellOrders = new ArrayList<>();
        }
        
        model.addAttribute("buyOrders", buyOrders);
        model.addAttribute("sellOrders", sellOrders);
        
        return "user/center";
    }
    
    @PostMapping("/cancel/{orderId}")
    @ResponseBody
    public Result<Void> cancelOrder(@PathVariable Long orderId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            orderService.cancelOrder(orderId, user.getUserId());
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 