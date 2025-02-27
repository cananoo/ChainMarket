package com.chainmarket.service;

import org.springframework.web.multipart.MultipartFile;

public interface IImageService {
    /**
     * 上传图片到图床
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(MultipartFile file);
} 