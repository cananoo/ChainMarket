package com.chainmarket.controller;

import com.chainmarket.entity.User;
import com.chainmarket.common.Result;
import com.chainmarket.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * 跳转到注册页面
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 获取验证码
     */
    @PostMapping("/verifyCode")
    @ResponseBody
    public Result<String> getVerifyCode(@RequestParam String phone) {
        try {
            String verifyCode = userService.sendVerifyCode(phone);
            return Result.success("验证码发送成功");
        } catch (Exception e) {
            return Result.error("验证码发送失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ResponseBody
    public Result<Void> register(@RequestBody User user, @RequestParam String verifyCode) {
        try {
            userService.register(user, verifyCode);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }
} 