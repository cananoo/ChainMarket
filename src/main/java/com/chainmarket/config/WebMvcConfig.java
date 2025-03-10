package com.chainmarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:src/main/resources/static/uploads}")
    private String uploadPath;
    
    @Value("${file.access.path:/uploads}")
    private String accessPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加资源处理器，将URL路径映射到文件系统路径
        registry.addResourceHandler(accessPath + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
} 