package com.chainmarket.controller;

import com.chainmarket.entity.User;
import com.chainmarket.service.IWalletService;
import com.chainmarket.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    
    @Autowired
    private IWalletService walletService;
    
    @GetMapping("/balance")
    public Result<BigDecimal> getBalance(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        try {
            BigDecimal balance = walletService.getBalance(user.getWalletAddress());
            return Result.success(balance);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/recharge")
    public Result<Void> recharge(@RequestBody Map<String, BigDecimal> params, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("请先登录");
        }
        
        BigDecimal amount = params.get("amount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("请输入正确的充值金额");
        }
        
        try {
            walletService.recharge(user.getWalletAddress(), amount);
            return Result.success("充值成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
} 