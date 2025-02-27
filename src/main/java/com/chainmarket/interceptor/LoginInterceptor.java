package com.chainmarket.interceptor;

import com.chainmarket.entity.User;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        // 如果用户未登录，重定向到登录页面
        if (user == null) {
            response.sendRedirect("/user/login");
            return false;
        }
        
        // 如果是管理相关的请求，检查用户是否为管理员
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/admin") || requestURI.startsWith("/audit")) {
            if (user.getRoleType() != 9) { // roleType=9为管理员
                response.sendError(403, "无权限执行此操作");
                return false;
            }
        }
        
        return true;
    }
} 