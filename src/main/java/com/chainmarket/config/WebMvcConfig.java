package com.chainmarket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:src/main/resources/static/uploads}") // :前从yml文件中读取，若无则读取后默认值
    private String uploadPath;
    
    @Value("${file.access.path:/uploads}")
    private String accessPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加资源处理器，通过浏览器访问的 URL 路径（如/uploads/image.jpg）
        // 映射到服务器上的文件系统路径（如src/main/resources/static/uploads/image.jpg）
        registry.addResourceHandler(accessPath + "/**")
                .addResourceLocations("file:" + uploadPath + "/"); // file:前缀表示这是一个文件系统路径
    }
} 