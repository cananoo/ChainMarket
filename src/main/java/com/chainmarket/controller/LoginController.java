package com.chainmarket.controller;

import com.chainmarket.dto.LoginDTO;
import com.chainmarket.entity.User;
import com.chainmarket.service.IUserService;
import com.chainmarket.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private IUserService userService;
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    @ResponseBody
    public Result<Void> login(@RequestBody LoginDTO loginDTO, HttpSession session) {
        try {
            User user = userService.login(loginDTO);
            // 将用户信息存入session
            session.setAttribute("user", user);
            return Result.success("登录成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    @ResponseBody
    public Result<Void> logout(HttpSession session) {
        session.removeAttribute("user");
        return Result.success("退出成功");
    }
} 