package com.chainmarket.service.impl;

import com.chainmarket.service.IImageService;
import com.chainmarket.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageServiceImpl implements IImageService {
    
    @Value("${file.upload.path:src/main/resources/static/uploads}")
    private String uploadPath;
    
    @Value("${file.access.path:/uploads}")
    private String accessPath;
    
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file == null || file.isEmpty()) {
                throw new BusinessException("上传的文件不能为空");
            }
            
            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException("只能上传图片文件");
            }
            
            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            if (extension == null) {
                extension = "jpg"; // 默认扩展名
            }
            
            // 创建目录（如果不存在）
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs()) {
                    throw new BusinessException("创建上传目录失败");
                }
            }
            
            // 生成唯一文件名
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            
            // 保存文件
            Path targetPath = Paths.get(uploadPath, newFilename);
            Files.copy(file.getInputStream(), targetPath);
            
            // 返回访问URL
            return accessPath + "/" + newFilename;
            
        } catch (IOException e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }
} 