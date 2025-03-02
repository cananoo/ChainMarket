package com.chainmarket.controller;

import com.chainmarket.dto.OrderReviewDTO;
import com.chainmarket.entity.User;
import com.chainmarket.service.IReviewService;
import com.chainmarket.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/review")
public class ReviewController {
    
    @Autowired
    private IReviewService reviewService;
    
    @PostMapping("/submit")
    @ResponseBody
    public Result<Void> submitReview(@RequestBody OrderReviewDTO reviewDTO, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        try {
            reviewService.submitReview(reviewDTO, user.getUserId());
            return Result.success("评价成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/order/{orderId}")
    @ResponseBody
    public Result<?> getOrderReview(@PathVariable Long orderId) {
        return Result.success(reviewService.getOrderReview(orderId));
    }
    
    @GetMapping("/seller/{sellerId}")
    @ResponseBody
    public Result<?> getSellerReviews(@PathVariable Long sellerId) {
        return Result.success(reviewService.getSellerReviews(sellerId));
    }
    
    @GetMapping("/buyer/{buyerId}")
    @ResponseBody
    public Result<?> getBuyerReviews(@PathVariable Long buyerId) {
        return Result.success(reviewService.getBuyerReviews(buyerId));
    }
}